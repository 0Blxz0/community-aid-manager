package org.communityaid.service

import munit.FunSuite

final class InputValidatorSuite extends FunSuite:
  test("required text rejects blank names") {
    assert(InputValidator.requiredText("   ", "Full name").isLeft)
  }

  test("money validation rejects negative values") {
    assertEquals(
      InputValidator.nonNegativeMoney("-1", "Monthly income"),
      Left("Monthly income cannot be negative.")
    )
  }

  test("dependant validation rejects an unrealistic number") {
    assertEquals(
      InputValidator.dependants("25"),
      Left("Dependants must be between 0 and 20.")
    )
  }

  test("valid money is parsed without throwing") {
    assertEquals(InputValidator.positiveMoney("125.50", "Amount"), Right(BigDecimal("125.50")))
  }
