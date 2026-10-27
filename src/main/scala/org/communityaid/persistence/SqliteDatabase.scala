package org.communityaid.persistence

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.sql.{Connection, DriverManager}
import scala.util.{Try, Using}
import scala.util.control.NonFatal

final class SqliteDatabase(
    private val databasePath: Path,
    private val schemaResource: String = "/schema.sql",
    private val sampleDataResource: Option[String] = Some("/sample-data.sql")
):
  private val databaseName = databasePath.getFileName.toString
  private val connectionUrl = s"jdbc:sqlite:${databasePath.toAbsolutePath}"
  private lazy val initialisationResult: Either[String, Unit] = initialise()

  def withConnection[Result](operation: Connection => Result): Either[String, Result] =
    initialisationResult.flatMap { _ =>
      Try(Using.resource(openConnection())(operation))
        .toEither
        .left
        .map(error => s"Could not access $databaseName: ${errorMessage(error)}")
    }

  private def initialise(): Either[String, Unit] =
    for
      schemaStatements <- loadStatements(schemaResource)
      sampleStatements <- sampleDataResource match
        case Some(resourceName) => loadStatements(resourceName)
        case None => Right(Vector.empty)
      result <- Try {
        Option(databasePath.toAbsolutePath.getParent).foreach(parent => Files.createDirectories(parent))
        Class.forName("org.sqlite.JDBC")
        Using.resource(openConnection()) { connection =>
          connection.setAutoCommit(false)
          try
            executeStatements(connection, schemaStatements)
            if !hasSeedMarker(connection) then
              executeStatements(connection, sampleStatements)
              markSeeded(connection)
            connection.commit()
          catch
            case NonFatal(error) =>
              connection.rollback()
              throw error
          finally connection.setAutoCommit(true)
        }
      }.toEither.left.map(error => s"Could not initialise $databaseName: ${errorMessage(error)}")
    yield result

  private def openConnection(): Connection =
    val connection = DriverManager.getConnection(connectionUrl)
    Using.resource(connection.createStatement())(_.execute("PRAGMA foreign_keys = ON"))
    connection

  private def loadStatements(resourceName: String): Either[String, Vector[String]] =
    Option(getClass.getResourceAsStream(resourceName))
      .toRight(s"Missing database resource: $resourceName")
      .flatMap { resourceStream =>
        Try {
          val sqlText =
            Using.resource(resourceStream)(stream => String(stream.readAllBytes(), StandardCharsets.UTF_8))
          sqlText.linesIterator
            .filterNot(line => line.trim.startsWith("--"))
            .mkString(System.lineSeparator())
            .split(";")
            .toVector
            .map(_.trim)
            .filter(_.nonEmpty)
        }.toEither.left.map(error => s"Could not read $resourceName: ${errorMessage(error)}")
      }

  private def executeStatements(connection: Connection, statements: Vector[String]): Unit =
    Using.resource(connection.createStatement()) { statement =>
      statements.foreach(sql => statement.execute(sql))
    }

  private def hasSeedMarker(connection: Connection): Boolean =
    val sql = "SELECT COUNT(*) FROM app_metadata WHERE metadata_key = ?"
    Using.resource(connection.prepareStatement(sql)) { statement =>
      statement.setString(1, "sample_seed_v1")
      Using.resource(statement.executeQuery()) { resultSet =>
        resultSet.next()
        resultSet.getInt(1) > 0
      }
    }

  private def markSeeded(connection: Connection): Unit =
    val sql = "INSERT INTO app_metadata (metadata_key, metadata_value) VALUES (?, ?)"
    Using.resource(connection.prepareStatement(sql)) { statement =>
      statement.setString(1, "sample_seed_v1")
      statement.setString(2, "complete")
      statement.executeUpdate()
      ()
    }

  private def errorMessage(error: Throwable): String =
    Option(error.getMessage).filter(_.trim.nonEmpty).getOrElse(error.getClass.getSimpleName)
