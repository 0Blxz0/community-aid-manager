package org.communityaid.service

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.util.Try

object ReportExporter:
  def writeReport(destination: Path, content: String): Either[String, Path] =
    Try {
      Option(destination.toAbsolutePath.getParent).foreach(parentPath => Files.createDirectories(parentPath))
      Files.write(destination, content.getBytes(StandardCharsets.UTF_8))
    }.toEither.left.map(error => s"Could not export report: ${error.getMessage}")
