package org.communityaid.ui

import scala.annotation.nowarn
import scalafx.Includes.*
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, ListView, ProgressBar}
import scalafx.scene.layout.{HBox, Priority, VBox}
import org.communityaid.controller.AppController
import org.communityaid.service.ReportService

@nowarn("cat=deprecation")
final class DashboardView(controller: AppController):
  private val beneficiaryTotal = new Label("0"):
    styleClass += "metric-value"

  private val eligibleTotal = new Label("0"):
    styleClass += "metric-value"

  private val priorityTotal = new Label("0"):
    styleClass += "metric-value"

  private val aidValueTotal = new Label("RM 0.00"):
    styleClass += "metric-value"

  private val eligibilityProgress = new ProgressBar:
    progress = 0.0
    prefWidth = 420

  private val recentItems = ObservableBuffer.empty[String]
  private val recentList = new ListView[String](recentItems):
    prefHeight = 230
    placeholder = new Label("No aid distributions have been recorded yet.")

  private val exportButton = new Button("Export summary report"):
    styleClass += "primary-button"
    onAction = handle {
      controller.exportSummaryReport() match
        case Right(exportedPath) =>
          UiSupport.showInformation("Report exported", exportedPath.toAbsolutePath.toString)
        case Left(message) => UiSupport.showError(message)
    }

  val root: VBox = new VBox:
    spacing = 18
    padding = Insets(24)
    children = Seq(
      UiSupport.pageTitle("Dashboard"),
      new Label("A clear snapshot of applicants, eligibility and delivered assistance."):
        styleClass += "page-subtitle",
      new HBox:
        spacing = 14
        children = Seq(
          metricCard("Beneficiaries", beneficiaryTotal),
          metricCard("Eligible", eligibleTotal),
          metricCard("Priority", priorityTotal),
          metricCard("Aid value", aidValueTotal)
        ),
      new VBox:
        spacing = 9
        styleClass += "content-card"
        children = Seq(
          UiSupport.sectionTitle("Eligibility coverage"),
          eligibilityProgress,
          new Label("Eligible and priority applicants as a proportion of all registered beneficiaries."):
            styleClass += "muted-label"
        ),
      new VBox:
        spacing = 10
        styleClass += "content-card"
        VBox.setVgrow(recentList, Priority.Always)
        children = Seq(UiSupport.sectionTitle("Recent distributions"), recentList, exportButton)
    )

  def refresh(): Unit =
    val beneficiaryCount = controller.beneficiaries.size
    val eligibleCount = ReportService.eligibleCount(controller.beneficiaries)
    beneficiaryTotal.text = beneficiaryCount.toString
    eligibleTotal.text = eligibleCount.toString
    priorityTotal.text = ReportService.priorityCount(controller.beneficiaries).toString
    aidValueTotal.text = f"RM ${ReportService.totalAidValue(controller.aidDistributions)}%.2f"
    eligibilityProgress.progress = if beneficiaryCount == 0 then 0.0 else eligibleCount.toDouble / beneficiaryCount

    recentItems.clear()
    recentItems ++= controller.aidDistributions.takeRight(8).reverse.map { distribution =>
      s"${distribution.distributedOn}  •  ${controller.beneficiaryName(distribution.beneficiaryId)}  •  ${distribution.aidType}  •  ${distribution.displayValue}"
    }

  private def metricCard(labelText: String, valueLabel: Label): VBox = new VBox:
    spacing = 5
    alignment = Pos.CenterLeft
    styleClass += "metric-card"
    HBox.setHgrow(this, Priority.Always)
    children = Seq(
      new Label(labelText):
        styleClass += "metric-label",
      valueLabel
    )
