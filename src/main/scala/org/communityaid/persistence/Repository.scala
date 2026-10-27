package org.communityaid.persistence

import org.communityaid.model.Identifiable

trait Repository[RecordType <: Identifiable]:
  def load(): Either[String, Vector[RecordType]]
  def save(records: Vector[RecordType]): Either[String, Unit]
