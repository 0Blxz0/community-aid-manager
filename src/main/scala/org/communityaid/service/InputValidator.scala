package org.communityaid.service

import scala.util.Try

object InputValidator:
  def requiredText(value: String, fieldName: String): Either[String, String] =
    Option(value).map(_.trim).filter(_.nonEmpty).toRight(s"$fieldName is required.")

  def nonNegativeMoney(value: String, fieldName: String): Either[String, BigDecimal] =
    Try(BigDecimal(value.trim)).toEither
      .left
      .map(_ => s"$fieldName must be a number.")
      .flatMap(amount => Either.cond(amount >= 0, amount, s"$fieldName cannot be negative."))

  def positiveMoney(value: String, fieldName: String): Either[String, BigDecimal] =
    nonNegativeMoney(value, fieldName)
      .flatMap(amount => Either.cond(amount > 0, amount, s"$fieldName must be greater than zero."))

  def dependants(value: String): Either[String, Int] =
    value.trim.toIntOption
      .toRight("Dependants must be a whole number.")
      .flatMap(count => Either.cond(count >= 0 && count <= 20, count, "Dependants must be between 0 and 20."))
