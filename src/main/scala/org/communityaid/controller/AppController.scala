package org.communityaid.controller

import java.nio.file.{Path, Paths}
import java.time.LocalDate
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import org.communityaid.model.*
import org.communityaid.persistence.Repository
import org.communityaid.service.{IdGenerator, InputValidator, ReportExporter, ReportService}

final class AppController(
    beneficiaryRepository: Repository[Beneficiary],
    aidRepository: Repository[Aid]
):
  val beneficiaries: ObservableBuffer[Beneficiary] = ObservableBuffer.empty[Beneficiary]
  val aidDistributions: ObservableBuffer[Aid] = ObservableBuffer.empty[Aid]
  val statusMessage: StringProperty = StringProperty("Ready")

  loadInitialData()

  def addBeneficiary(
      beneficiaryType: String,
      fullNameText: String,
      incomeText: String,
      dependantsText: String,
      employmentStatus: EmploymentStatus,
      locationText: String
  ): Either[String, Beneficiary] =
    for
      fullName <- InputValidator.requiredText(fullNameText, "Full name")
      income <- InputValidator.nonNegativeMoney(incomeText, "Monthly income")
      dependantCount <- InputValidator.dependants(dependantsText)
      location <- InputValidator.requiredText(locationText, "Location")
      beneficiary <- createBeneficiary(
        beneficiaryType,
        IdGenerator.next("BEN", beneficiaries.map(_.id)),
        fullName,
        income,
        dependantCount,
        employmentStatus,
        location
      )
      saved <- saveBeneficiaries(beneficiaries.toVector :+ beneficiary)
    yield
      beneficiaries += saved
      statusMessage.value = s"Added ${saved.fullName}."
      saved

  def updateBeneficiary(
      identifier: String,
      beneficiaryType: String,
      fullNameText: String,
      incomeText: String,
      dependantsText: String,
      employmentStatus: EmploymentStatus,
      locationText: String
  ): Either[String, Beneficiary] =
    val existingIndex = beneficiaries.indexWhere(_.id == identifier)
    if existingIndex < 0 then Left("Select an existing beneficiary before updating.")
    else
      for
        fullName <- InputValidator.requiredText(fullNameText, "Full name")
        income <- InputValidator.nonNegativeMoney(incomeText, "Monthly income")
        dependantCount <- InputValidator.dependants(dependantsText)
        location <- InputValidator.requiredText(locationText, "Location")
        replacement <- createBeneficiary(
          beneficiaryType,
          identifier,
          fullName,
          income,
          dependantCount,
          employmentStatus,
          location
        )
        saved <- saveBeneficiaries(beneficiaries.toVector.updated(existingIndex, replacement))
      yield
        beneficiaries.update(existingIndex, saved)
        statusMessage.value = s"Updated ${saved.fullName}."
        saved

  def deleteBeneficiary(identifier: String): Either[String, Unit] =
    if aidDistributions.exists(_.beneficiaryId == identifier) then
      Left("This beneficiary has distribution records and cannot be deleted.")
    else
      val remaining = beneficiaries.filterNot(_.id == identifier).toVector
      if remaining.size == beneficiaries.size then Left("Select a beneficiary to delete.")
      else
        saveBeneficiaryRecords(remaining).map { _ =>
          beneficiaries.remove(beneficiaries.indexWhere(_.id == identifier))
          statusMessage.value = s"Deleted beneficiary $identifier."
        }

  def registerCashAid(
      beneficiary: Beneficiary,
      amountText: String,
      distributedOn: LocalDate,
      distributionStatus: DistributionStatus
  ): Either[String, Aid] =
    for
      _ <- requireEligible(beneficiary)
      amount <- InputValidator.positiveMoney(amountText, "Cash amount")
      _ <- requireUniqueDistribution(beneficiary.id, "Cash", distributedOn)
      aid = CashAid(
        IdGenerator.next("AID", aidDistributions.map(_.id)),
        beneficiary.id,
        amount,
        distributedOn,
        distributionStatus
      )
      saved <- saveAid(aidDistributions.toVector :+ aid)
    yield
      aidDistributions += saved
      statusMessage.value = s"Recorded cash aid for ${beneficiary.fullName}."
      saved

  def registerFoodAid(
      beneficiary: Beneficiary,
      descriptionText: String,
      estimatedValueText: String,
      distributedOn: LocalDate,
      distributionStatus: DistributionStatus
  ): Either[String, Aid] =
    for
      _ <- requireEligible(beneficiary)
      description <- InputValidator.requiredText(descriptionText, "Food package description")
      estimatedValue <- InputValidator.positiveMoney(estimatedValueText, "Estimated value")
      _ <- requireUniqueDistribution(beneficiary.id, "Food", distributedOn)
      aid = FoodAid(
        IdGenerator.next("AID", aidDistributions.map(_.id)),
        beneficiary.id,
        description,
        estimatedValue,
        distributedOn,
        distributionStatus
      )
      saved <- saveAid(aidDistributions.toVector :+ aid)
    yield
      aidDistributions += saved
      statusMessage.value = s"Recorded food aid for ${beneficiary.fullName}."
      saved

  def beneficiaryName(identifier: String): String =
    beneficiaries.find(_.id == identifier).map(_.fullName).getOrElse(s"Unknown ($identifier)")

  def exportSummaryReport(): Either[String, Path] =
    val destination = Paths.get("reports", "aid-summary.txt")
    val content = ReportService.renderSummary(beneficiaries, aidDistributions)
    ReportExporter.writeReport(destination, content).map { exportedPath =>
      statusMessage.value = s"Exported report to ${exportedPath.toAbsolutePath}."
      exportedPath
    }

  private def createBeneficiary(
      beneficiaryType: String,
      identifier: String,
      fullName: String,
      income: BigDecimal,
      dependants: Int,
      employmentStatus: EmploymentStatus,
      location: String
  ): Either[String, Beneficiary] = beneficiaryType match
    case "Household" =>
      Right(Household(identifier, fullName, income, dependants, employmentStatus, location))
    case "Individual" =>
      Right(Individual(identifier, fullName, income, employmentStatus, location))
    case unsupported => Left(s"Unsupported beneficiary type: $unsupported")

  private def requireEligible(beneficiary: Beneficiary): Either[String, Unit] =
    Either.cond(
      beneficiary.eligibilityStatus != EligibilityStatus.NotEligible,
      (),
      s"${beneficiary.fullName} is not currently eligible for aid."
    )

  // ai-assisted: #10
  // why: AI's SQL test-plan review identified duplicate daily aid as an important case, so the controller gives a friendly error before the database constraint is reached.
  private def requireUniqueDistribution(
      beneficiaryId: String,
      aidType: String,
      distributedOn: LocalDate
  ): Either[String, Unit] =
    val duplicateExists = aidDistributions.exists(distribution =>
      distribution.beneficiaryId == beneficiaryId &&
        distribution.aidType == aidType &&
        distribution.distributedOn == distributedOn
    )
    Either.cond(!duplicateExists, (), "The same aid type has already been recorded for this beneficiary today.")

  private def saveBeneficiaries(records: Vector[Beneficiary]): Either[String, Beneficiary] =
    records.lastOption.toRight("No beneficiary was provided.").flatMap { lastRecord =>
      saveBeneficiaryRecords(records).map(_ => lastRecord)
    }

  private def saveBeneficiaryRecords(records: Vector[Beneficiary]): Either[String, Unit] =
    beneficiaryRepository.save(records).left.map(handleStorageError)

  private def saveAid(records: Vector[Aid]): Either[String, Aid] =
    records.lastOption.toRight("No aid record was provided.").flatMap { lastRecord =>
      aidRepository.save(records).left.map(handleStorageError).map(_ => lastRecord)
    }

  private def handleStorageError(message: String): String =
    statusMessage.value = message
    message

  private def loadInitialData(): Unit =
    beneficiaryRepository.load() match
      case Right(records) => beneficiaries ++= records
      case Left(message) => statusMessage.value = message

    aidRepository.load() match
      case Right(records) => aidDistributions ++= records
      case Left(message) => statusMessage.value = message
