package org.communityaid.persistence

import java.sql.{PreparedStatement, ResultSet}
import org.communityaid.model.Identifiable

trait SqlTable[RecordType <: Identifiable]:
  def tableName: String
  def selectSql: String
  def upsertSql: String
  def bindUpsert(statement: PreparedStatement, record: RecordType): Unit
  def readRecord(resultSet: ResultSet): Either[String, RecordType]
