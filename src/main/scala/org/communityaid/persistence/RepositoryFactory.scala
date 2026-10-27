package org.communityaid.persistence

import java.nio.file.{Path, Paths}
import org.communityaid.model.{Aid, Beneficiary}

object RepositoryFactory:
  // ai-assisted: #9
  // why: SQLite gives the desktop app real SQL persistence without requiring a separately installed server.
  private val databasePath: Path =
    Option(System.getProperty("communityaid.database.path"))
      .filter(_.trim.nonEmpty)
      .map(Paths.get(_))
      .getOrElse(defaultUserDatabasePath)

  private lazy val database = SqliteDatabase(databasePath)

  lazy val beneficiaryRepository: Repository[Beneficiary] =
    SqlRepository(database, BeneficiarySqlTable)

  lazy val aidRepository: Repository[Aid] =
    SqlRepository(database, AidSqlTable)

  private def defaultUserDatabasePath: Path =
    Option(System.getenv("LOCALAPPDATA"))
      .filter(_.trim.nonEmpty)
      .map(Paths.get(_))
      .getOrElse(Paths.get(System.getProperty("user.home")))
      .resolve("CommunityAidManager")
      .resolve("data")
      .resolve("community-aid.db")
