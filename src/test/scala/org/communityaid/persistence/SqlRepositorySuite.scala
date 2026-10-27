package org.communityaid.persistence

import java.nio.file.Files
import java.time.LocalDate
import munit.FunSuite
import org.communityaid.model.*

final class SqlRepositorySuite extends FunSuite:
  test("a new SQL database is created with the bundled sample records") {
    val databasePath = Files.createTempDirectory("community-aid-sql-test").resolve("seeded.db")
    val database = SqliteDatabase(databasePath)
    val beneficiaryRepository = SqlRepository(database, BeneficiarySqlTable)
    val aidRepository = SqlRepository(database, AidSqlTable)

    assertEquals(beneficiaryRepository.load().map(_.size), Right(3))
    assertEquals(aidRepository.load().map(_.size), Right(2))
    assert(Files.exists(databasePath))
  }

  test("generic SQL repositories round-trip beneficiaries and related aid") {
    val databasePath = Files.createTempDirectory("community-aid-sql-test").resolve("round-trip.db")
    val database = SqliteDatabase(databasePath, sampleDataResource = None)
    val beneficiaryRepository = SqlRepository(database, BeneficiarySqlTable)
    val aidRepository = SqlRepository(database, AidSqlTable)
    val beneficiary = Household(
      "BEN-101",
      "A \"Priority\", Family",
      BigDecimal("950.50"),
      3,
      EmploymentStatus.Unemployed,
      "Shah Alam, Selangor"
    )
    val aid = FoodAid(
      "AID-101",
      beneficiary.id,
      "Rice, oil and milk",
      BigDecimal("125.75"),
      LocalDate.of(2026, 7, 27),
      DistributionStatus.Distributed
    )

    assertEquals(beneficiaryRepository.save(Vector(beneficiary)), Right(()))
    assertEquals(aidRepository.save(Vector(aid)), Right(()))
    assertEquals(beneficiaryRepository.load(), Right(Vector(beneficiary)))
    assertEquals(aidRepository.load(), Right(Vector(aid)))
    assert(Files.exists(databasePath))
  }

  test("SQL foreign key rejects aid for an unknown beneficiary") {
    val databasePath = Files.createTempDirectory("community-aid-sql-test").resolve("foreign-key.db")
    val database = SqliteDatabase(databasePath, sampleDataResource = None)
    val aidRepository = SqlRepository(database, AidSqlTable)
    val invalidAid = CashAid(
      "AID-201",
      "BEN-DOES-NOT-EXIST",
      BigDecimal("100.00"),
      LocalDate.of(2026, 7, 27),
      DistributionStatus.Scheduled
    )

    assert(aidRepository.save(Vector(invalidAid)).isLeft)
  }

  test("SQL unique constraint rejects duplicate daily aid") {
    val databasePath = Files.createTempDirectory("community-aid-sql-test").resolve("unique.db")
    val database = SqliteDatabase(databasePath, sampleDataResource = None)
    val beneficiaryRepository = SqlRepository(database, BeneficiarySqlTable)
    val aidRepository = SqlRepository(database, AidSqlTable)
    val beneficiary =
      Individual("BEN-301", "Test Applicant", BigDecimal("900"), EmploymentStatus.Unemployed, "Klang")
    val firstAid = CashAid(
      "AID-301",
      beneficiary.id,
      BigDecimal("50"),
      LocalDate.of(2026, 7, 27),
      DistributionStatus.Distributed
    )
    val duplicateAid = firstAid.copy(id = "AID-302")

    assertEquals(beneficiaryRepository.save(Vector(beneficiary)), Right(()))
    assert(aidRepository.save(Vector(firstAid, duplicateAid)).isLeft)
    assertEquals(aidRepository.load(), Right(Vector.empty))
  }
