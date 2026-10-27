package org.communityaid.service

import org.communityaid.model.*

object ReportService:
  def eligibleCount(beneficiaries: Iterable[Beneficiary]): Int =
    beneficiaries.count(_.eligibilityStatus != EligibilityStatus.NotEligible)

  def priorityCount(beneficiaries: Iterable[Beneficiary]): Int =
    beneficiaries.count(_.eligibilityStatus == EligibilityStatus.Priority)

  def totalAidValue(aidDistributions: Iterable[Aid]): BigDecimal =
    aidDistributions.map(_.estimatedValue).sum

  def cashAidValue(aidDistributions: Iterable[Aid]): BigDecimal =
    aidDistributions.collect { case cashAid: CashAid => cashAid.amount }.sum

  def foodPackageCount(aidDistributions: Iterable[Aid]): Int =
    aidDistributions.count(_.isInstanceOf[FoodAid])

  def renderSummary(
      beneficiaries: Iterable[Beneficiary],
      aidDistributions: Iterable[Aid]
  ): String =
    val beneficiaryRecords = beneficiaries.toVector
    val aidRecords = aidDistributions.toVector
    val distributionLines = aidRecords.sortBy(_.distributedOn).map { distribution =>
      s"${distribution.distributedOn} | ${distribution.beneficiaryId} | ${distribution.aidType} | ${distribution.displayValue} | ${distribution.status.label}"
    }

    Vector(
      "COMMUNITY AID SUMMARY",
      "=====================",
      s"Registered beneficiaries: ${beneficiaryRecords.size}",
      s"Eligible beneficiaries: ${eligibleCount(beneficiaryRecords)}",
      s"Priority beneficiaries: ${priorityCount(beneficiaryRecords)}",
      f"Total estimated aid value: RM ${totalAidValue(aidRecords)}%.2f",
      f"Cash distributed: RM ${cashAidValue(aidRecords)}%.2f",
      s"Food packages: ${foodPackageCount(aidRecords)}",
      "",
      "DISTRIBUTION HISTORY",
      "--------------------"
    ).mkString(System.lineSeparator()) + System.lineSeparator() +
      distributionLines.mkString(System.lineSeparator())
