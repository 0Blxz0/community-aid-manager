package org.communityaid.service

object IdGenerator:
  def next(prefix: String, existingIds: Iterable[String]): String =
    val highestNumber = existingIds
      .flatMap(identifier => identifier.stripPrefix(s"$prefix-").toIntOption)
      .maxOption
      .getOrElse(0)

    f"$prefix-${highestNumber + 1}%03d"
