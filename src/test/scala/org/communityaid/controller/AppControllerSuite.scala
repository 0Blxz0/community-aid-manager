package org.communityaid.controller

import java.time.LocalDate
import munit.FunSuite
import org.communityaid.model.*
import org.communityaid.persistence.Repository

final class AppControllerSuite extends FunSuite:
  private final class InMemoryRepository[RecordType <: Identifiable](initial: Vector[RecordType])
      extends Repository[RecordType]:
    private var storedRecords = initial
    override def load(): Either[String, Vector[RecordType]] = Right(storedRecords)
    override def save(records: Vector[RecordType]): Either[String, Unit] =
      storedRecords = records
      Right(())

  test("controller registers and persists an eligible beneficiary") {
    val beneficiaryRepository = InMemoryRepository[Beneficiary](Vector.empty)
    val aidRepository = InMemoryRepository[Aid](Vector.empty)
    val controller = AppController(beneficiaryRepository, aidRepository)

    val result = controller.addBeneficiary(
      beneficiaryType = "Household",
      fullNameText = "Test Family",
      incomeText = "1000",
      dependantsText = "3",
      employmentStatus = EmploymentStatus.Unemployed,
      locationText = "Selangor"
    )

    assert(result.isRight)
    assertEquals(controller.beneficiaries.size, 1)
    assertEquals(beneficiaryRepository.load().map(_.size), Right(1))
  }

  test("controller rejects aid for an ineligible applicant") {
    val applicant = Individual(
      "BEN-001",
      "Higher Income Applicant",
      BigDecimal(5000),
      EmploymentStatus.Employed,
      "Selangor"
    )
    val beneficiaryRepository = InMemoryRepository[Beneficiary](Vector(applicant))
    val aidRepository = InMemoryRepository[Aid](Vector.empty)
    val controller = AppController(beneficiaryRepository, aidRepository)

    val result = controller.registerCashAid(
      applicant,
      "100",
      LocalDate.of(2026, 7, 20),
      DistributionStatus.Distributed
    )

    assert(result.isLeft)
    assertEquals(controller.aidDistributions.size, 0)
  }
