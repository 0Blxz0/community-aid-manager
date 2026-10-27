package org.communityaid.persistence

import java.sql.{PreparedStatement, ResultSet}
import java.time.LocalDate
import scala.util.Try
import org.communityaid.model.*

object BeneficiarySqlTable extends SqlTable[Beneficiary]:
  override val tableName: String = "beneficiaries"

  override val selectSql: String =
    """SELECT id, beneficiary_type, full_name, monthly_income, dependants,
      |       employment_status, location
      |FROM beneficiaries
      |ORDER BY id""".stripMargin

  override val upsertSql: String =
    """INSERT INTO beneficiaries (
      |    id, beneficiary_type, full_name, monthly_income, dependants,
      |    employment_status, location
      |) VALUES (?, ?, ?, ?, ?, ?, ?)
      |ON CONFLICT(id) DO UPDATE SET
      |    beneficiary_type = excluded.beneficiary_type,
      |    full_name = excluded.full_name,
      |    monthly_income = excluded.monthly_income,
      |    dependants = excluded.dependants,
      |    employment_status = excluded.employment_status,
      |    location = excluded.location""".stripMargin

  override def bindUpsert(statement: PreparedStatement, beneficiary: Beneficiary): Unit =
    statement.setString(1, beneficiary.id)
    statement.setString(2, beneficiary.beneficiaryType)
    statement.setString(3, beneficiary.fullName)
    statement.setBigDecimal(4, beneficiary.monthlyIncome.bigDecimal)
    statement.setInt(5, beneficiary.dependants)
    statement.setString(6, beneficiary.employmentStatus.label)
    statement.setString(7, beneficiary.location)

  override def readRecord(resultSet: ResultSet): Either[String, Beneficiary] =
    val identifier = resultSet.getString("id")
    val beneficiaryType = resultSet.getString("beneficiary_type")
    for
      income <- SqlValueParser.decimal(resultSet.getString("monthly_income"), "monthly income", identifier)
      employment <- EmploymentStatus
        .fromLabel(resultSet.getString("employment_status"))
        .left
        .map(message => s"$message in beneficiary $identifier")
      beneficiary <- beneficiaryType match
        case "Household" =>
          Right(
            Household(
              identifier,
              resultSet.getString("full_name"),
              income,
              resultSet.getInt("dependants"),
              employment,
              resultSet.getString("location")
            )
          )
        case "Individual" =>
          Right(
            Individual(
              identifier,
              resultSet.getString("full_name"),
              income,
              employment,
              resultSet.getString("location")
            )
          )
        case unsupported => Left(s"Unsupported beneficiary type '$unsupported' in beneficiary $identifier")
    yield beneficiary

object AidSqlTable extends SqlTable[Aid]:
  override val tableName: String = "aid_distributions"

  override val selectSql: String =
    """SELECT id, aid_type, beneficiary_id, aid_value, package_description,
      |       distributed_on, distribution_status
      |FROM aid_distributions
      |ORDER BY distributed_on, id""".stripMargin

  override val upsertSql: String =
    """INSERT INTO aid_distributions (
      |    id, aid_type, beneficiary_id, aid_value, package_description,
      |    distributed_on, distribution_status
      |) VALUES (?, ?, ?, ?, ?, ?, ?)
      |ON CONFLICT(id) DO UPDATE SET
      |    aid_type = excluded.aid_type,
      |    beneficiary_id = excluded.beneficiary_id,
      |    aid_value = excluded.aid_value,
      |    package_description = excluded.package_description,
      |    distributed_on = excluded.distributed_on,
      |    distribution_status = excluded.distribution_status""".stripMargin

  override def bindUpsert(statement: PreparedStatement, aid: Aid): Unit =
    statement.setString(1, aid.id)
    statement.setString(2, aid.aidType)
    statement.setString(3, aid.beneficiaryId)
    statement.setBigDecimal(4, aid.estimatedValue.bigDecimal)
    statement.setString(
      5,
      aid match
        case foodAid: FoodAid => foodAid.packageDescription
        case _: CashAid => ""
    )
    statement.setString(6, aid.distributedOn.toString)
    statement.setString(7, aid.status.label)

  override def readRecord(resultSet: ResultSet): Either[String, Aid] =
    val identifier = resultSet.getString("id")
    val aidType = resultSet.getString("aid_type")
    for
      value <- SqlValueParser.decimal(resultSet.getString("aid_value"), "aid value", identifier)
      distributedOn <- SqlValueParser.date(resultSet.getString("distributed_on"), identifier)
      status <- DistributionStatus
        .fromLabel(resultSet.getString("distribution_status"))
        .left
        .map(message => s"$message in aid record $identifier")
      aid <- aidType match
        case "Cash" =>
          Right(CashAid(identifier, resultSet.getString("beneficiary_id"), value, distributedOn, status))
        case "Food" =>
          Right(
            FoodAid(
              identifier,
              resultSet.getString("beneficiary_id"),
              resultSet.getString("package_description"),
              value,
              distributedOn,
              status
            )
          )
        case unsupported => Left(s"Unsupported aid type '$unsupported' in aid record $identifier")
    yield aid

private object SqlValueParser:
  def decimal(value: String, fieldName: String, identifier: String): Either[String, BigDecimal] =
    Try(BigDecimal(value)).toEither.left.map(_ => s"Invalid $fieldName in record $identifier")

  def date(value: String, identifier: String): Either[String, LocalDate] =
    Try(LocalDate.parse(value)).toEither.left.map(_ => s"Invalid distribution date in aid record $identifier")
