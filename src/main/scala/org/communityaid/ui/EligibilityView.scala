package org.communityaid.ui

import scala.annotation.nowarn
import scalafx.Includes.*
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, ComboBox, Label, ListView, ProgressBar}
import scalafx.scene.layout.{HBox, Priority, VBox}
import org.communityaid.controller.AppController
import org.communityaid.model.{Beneficiary, EligibilityStatus, Household}
import org.communityaid.service.EligibilityCalculator

@nowarn("cat=deprecation")
final class EligibilityView(controller: AppController):
  private val beneficiaryChoice = new ComboBox[Beneficiary](controller.beneficiaries):
    promptText = "Choose a beneficiary"
    prefWidth = 360

  private val scoreValue = new Label("--"):
    styleClass += "score-value"

  private val statusValue = new Label("Select a beneficiary"):
    styleClass += "status-chip"

  private val scoreProgress = new ProgressBar:
    progress = 0.0
    prefWidth = 420

  private val breakdownItems = ObservableBuffer.empty[String]
  private val breakdownList = new ListView[String](breakdownItems):
    prefHeight = 220

  private val calculateButton = new Button("Calculate eligibility"):
    defaultButton = true
    styleClass += "primary-button"
    onAction = handle { calculate() }

  val root: VBox = new VBox:
    spacing = 18
    padding = Insets(24)
    children = Seq(
      UiSupport.pageTitle("Eligibility checker"),
      new Label("Apply one transparent policy and show exactly how the score was produced."):
        styleClass += "page-subtitle",
      new HBox:
        spacing = 12
        children = Seq(beneficiaryChoice, calculateButton),
      new HBox:
        spacing = 18
        children = Seq(
          new VBox:
            spacing = 8
            styleClass += "score-card"
            children = Seq(new Label("Eligibility score"), scoreValue, scoreProgress, statusValue),
          new VBox:
            spacing = 10
            styleClass += "content-card"
            HBox.setHgrow(this, Priority.Always)
            children = Seq(
              UiSupport.sectionTitle("How the score is calculated"),
              breakdownList,
              new Label("Priority: 70–100  •  Eligible: 45–69  •  Not eligible: 0–44"):
                styleClass += "muted-label"
            )
        )
    )

  def refresh(): Unit =
    if beneficiaryChoice.value.value == null && controller.beneficiaries.nonEmpty then
      beneficiaryChoice.selectionModel().selectFirst()

  // ai-assisted: #5
  // why: AI's scoring review helped display the tested calculation and its edge-case-safe breakdown without duplicating the formula.
  private def calculate(): Unit =
    Option(beneficiaryChoice.value.value) match
      case None => UiSupport.showError("Select a beneficiary first.")
      case Some(beneficiary) =>
        val score = beneficiary.eligibilityScore
        val breakdown = EligibilityCalculator.explanation(
          beneficiary.monthlyIncome,
          beneficiary.dependants,
          beneficiary.employmentStatus,
          beneficiary.isInstanceOf[Household]
        )

        scoreValue.text = s"$score / 100"
        scoreProgress.progress = score.toDouble / 100
        statusValue.text = beneficiary.eligibilityStatus.label
        statusValue.styleClass.removeAll("priority-status", "eligible-status", "ineligible-status")
        statusValue.styleClass += (beneficiary.eligibilityStatus match
          case EligibilityStatus.Priority => "priority-status"
          case EligibilityStatus.Eligible => "eligible-status"
          case EligibilityStatus.NotEligible => "ineligible-status")

        breakdownItems.clear()
        breakdownItems ++= breakdown.map { case (reason, points) => f"$reason%-25s +$points%2d points" }
