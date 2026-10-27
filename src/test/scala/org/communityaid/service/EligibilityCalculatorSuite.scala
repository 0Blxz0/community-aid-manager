package org.communityaid.service

import munit.FunSuite
import org.communityaid.model.{EligibilityStatus, EmploymentStatus}

final class EligibilityCalculatorSuite extends FunSuite:
  test("a low-income household with dependants receives priority status") {
    val score = EligibilityCalculator.householdScore(
      monthlyIncome = BigDecimal(1200),
      dependants = 4,
      employmentStatus = EmploymentStatus.PartTime
    )

    assertEquals(score, 79)
    assertEquals(EligibilityCalculator.statusFor(score), EligibilityStatus.Priority)
  }

  test("a higher-income employed individual is not eligible") {
    val score = EligibilityCalculator.individualScore(
      monthlyIncome = BigDecimal(4500),
      employmentStatus = EmploymentStatus.Employed
    )

    assertEquals(score, 0)
    assertEquals(EligibilityCalculator.statusFor(score), EligibilityStatus.NotEligible)
  }

  test("eligibility scores are clamped at one hundred") {
    val score = EligibilityCalculator.householdScore(
      monthlyIncome = BigDecimal(500),
      dependants = 20,
      employmentStatus = EmploymentStatus.Unemployed
    )

    assertEquals(score, 100)
  }
