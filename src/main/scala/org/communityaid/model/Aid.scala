package org.communityaid.model

import java.time.LocalDate

enum DistributionStatus(val label: String):
  case Scheduled extends DistributionStatus("Scheduled")
  case Distributed extends DistributionStatus("Distributed")

  override def toString: String = label

object DistributionStatus:
  def fromLabel(label: String): Either[String, DistributionStatus] =
    values.find(_.label == label).toRight(s"Unknown distribution status: $label")

sealed trait Aid extends Identifiable:
  def beneficiaryId: String
  def distributedOn: LocalDate
  def status: DistributionStatus
  def aidType: String
  def displayValue: String
  def estimatedValue: BigDecimal

final case class CashAid(
    override val id: String,
    override val beneficiaryId: String,
    amount: BigDecimal,
    override val distributedOn: LocalDate,
    override val status: DistributionStatus
) extends Aid:
  override val aidType: String = "Cash"
  override def displayValue: String = f"RM $amount%.2f"
  override def estimatedValue: BigDecimal = amount

final case class FoodAid(
    override val id: String,
    override val beneficiaryId: String,
    packageDescription: String,
    override val estimatedValue: BigDecimal,
    override val distributedOn: LocalDate,
    override val status: DistributionStatus
) extends Aid:
  override val aidType: String = "Food"
  override def displayValue: String = s"$packageDescription (RM ${estimatedValue.setScale(2)})"
