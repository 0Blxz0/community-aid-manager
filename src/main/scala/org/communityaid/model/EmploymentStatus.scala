package org.communityaid.model

enum EmploymentStatus(val label: String):
  case Employed extends EmploymentStatus("Employed")
  case PartTime extends EmploymentStatus("Part-time")
  case Unemployed extends EmploymentStatus("Unemployed")

  override def toString: String = label

object EmploymentStatus:
  def fromLabel(label: String): Either[String, EmploymentStatus] =
    values.find(_.label == label).toRight(s"Unknown employment status: $label")
