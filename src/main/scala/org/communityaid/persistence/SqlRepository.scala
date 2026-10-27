package org.communityaid.persistence

import java.sql.Connection
import scala.util.Using
import scala.util.control.NonFatal
import org.communityaid.model.Identifiable

final class SqlRepository[RecordType <: Identifiable](
    private val database: SqliteDatabase,
    private val table: SqlTable[RecordType]
) extends Repository[RecordType]:

  override def load(): Either[String, Vector[RecordType]] =
    database
      .withConnection { connection =>
        Using.resource(connection.prepareStatement(table.selectSql)) { statement =>
          Using.resource(statement.executeQuery()) { resultSet =>
            var records = Vector.empty[RecordType]
            var decodeError = Option.empty[String]
            while resultSet.next() && decodeError.isEmpty do
              table.readRecord(resultSet) match
                case Right(record) => records :+= record
                case Left(message) => decodeError = Some(message)
            decodeError.toLeft(records)
          }
        }
      }
      .flatMap(result => result)

  override def save(records: Vector[RecordType]): Either[String, Unit] =
    database.withConnection { connection =>
      connection.setAutoCommit(false)
      try
        upsertRecords(connection, records)
        deleteMissingRecords(connection, records.map(_.id).distinct)
        connection.commit()
      catch
        case NonFatal(error) =>
          connection.rollback()
          throw error
      finally connection.setAutoCommit(true)
    }

  private def upsertRecords(connection: Connection, records: Vector[RecordType]): Unit =
    if records.nonEmpty then
      Using.resource(connection.prepareStatement(table.upsertSql)) { statement =>
        records.foreach { record =>
          table.bindUpsert(statement, record)
          statement.addBatch()
        }
        statement.executeBatch()
        ()
      }

  private def deleteMissingRecords(connection: Connection, identifiers: Vector[String]): Unit =
    if identifiers.isEmpty then
      Using.resource(connection.prepareStatement(s"DELETE FROM ${table.tableName}")) { statement =>
        statement.executeUpdate()
        ()
      }
    else
      val placeholders = Vector.fill(identifiers.size)("?").mkString(", ")
      val sql = s"DELETE FROM ${table.tableName} WHERE id NOT IN ($placeholders)"
      Using.resource(connection.prepareStatement(sql)) { statement =>
        identifiers.zipWithIndex.foreach { case (identifier, index) =>
          statement.setString(index + 1, identifier)
        }
        statement.executeUpdate()
        ()
      }
