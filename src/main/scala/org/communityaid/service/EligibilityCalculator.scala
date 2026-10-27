package org.communityaid.service

import org.communityaid.model.{EligibilityStatus, EmploymentStatus}

object EligibilityCalculator:
  def householdScore(
      monthlyIncome: BigDecimal,
      dependants: Int,
      employmentStatus: EmploymentStatus
  ): Int =
    val incomePoints = pointsForIncome(monthlyIncome)
    val dependantPoints = math.min(dependants * 6, 30)
    val typeSupportPoints = 5
    clamp(incomePoints + dependantPoints + employmentPoints(employmentStatus) + typeSupportPoints)

  def individualScore(monthlyIncome: BigDecimal, employmentStatus: EmploymentStatus): Int =
    clamp(pointsForIncome(monthlyIncome) + employmentPoints(employmentStatus))

  def statusFor(score: Int): EligibilityStatus =
    if score >= 70 then EligibilityStatus.Priority
    else if score >= 45 then EligibilityStatus.Eligible
    else EligibilityStatus.NotEligible

  def explanation(
      monthlyIncome: BigDecimal,
      dependants: Int,
      employmentStatus: EmploymentStatus,
      isHousehold: Boolean
  ): Vector[(String, Int)] =
    Vector(
      "Income support" -> pointsForIncome(monthlyIncome),
      "Dependants" -> (if isHousehold then math.min(dependants * 6, 30) else 0),
      "Employment status" -> employmentPoints(employmentStatus),
      "Household support" -> (if isHousehold then 5 else 0)
    )

  private def pointsForIncome(monthlyIncome: BigDecimal): Int =
    if monthlyIncome <= 1000 then 55
    else if monthlyIncome <= 2000 then 40
    else if monthlyIncome <= 3000 then 25
    else if monthlyIncome <= 4000 then 10
    else 0

  private def employmentPoints(employmentStatus: EmploymentStatus): Int = employmentStatus match
    case EmploymentStatus.Unemployed => 20
    case EmploymentStatus.PartTime => 10
    case EmploymentStatus.Employed => 0

  private def clamp(score: Int): Int = math.max(0, math.min(100, score))
