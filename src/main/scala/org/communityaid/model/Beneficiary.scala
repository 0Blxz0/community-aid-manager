package org.communityaid.model

import org.communityaid.service.EligibilityCalculator

sealed trait Beneficiary extends Identifiable:
  def fullName: String
  def monthlyIncome: BigDecimal
  def dependants: Int
  def employmentStatus: EmploymentStatus
  def location: String
  def beneficiaryType: String
  def eligibilityScore: Int

  final def eligibilityStatus: EligibilityStatus =
    EligibilityCalculator.statusFor(eligibilityScore)

  final override def toString: String = s"$fullName ($id)"

final case class Household(
    override val id: String,
    override val fullName: String,
    override val monthlyIncome: BigDecimal,
    override val dependants: Int,
    override val employmentStatus: EmploymentStatus,
    override val location: String
) extends Beneficiary:
  override val beneficiaryType: String = "Household"

  // ai-assisted: #5
  // why: AI's edge-case review helped keep the household scoring rule capped and independently testable.
  override def eligibilityScore: Int =
    EligibilityCalculator.householdScore(monthlyIncome, dependants, employmentStatus)

final case class Individual(
    override val id: String,
    override val fullName: String,
    override val monthlyIncome: BigDecimal,
    override val employmentStatus: EmploymentStatus,
    override val location: String
) extends Beneficiary:
  override val dependants: Int = 0
  override val beneficiaryType: String = "Individual"

  override def eligibilityScore: Int =
    EligibilityCalculator.individualScore(monthlyIncome, employmentStatus)
