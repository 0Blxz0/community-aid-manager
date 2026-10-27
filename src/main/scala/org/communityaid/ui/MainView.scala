package org.communityaid.ui

import scala.annotation.nowarn
import scalafx.Includes.*
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, Separator}
import scalafx.scene.layout.{BorderPane, Priority, VBox}
import org.communityaid.controller.AppController

@nowarn("cat=deprecation")
final class MainView(controller: AppController):
  private val dashboardView = DashboardView(controller)
  private val beneficiariesView = BeneficiariesView(controller)
  private val eligibilityView = EligibilityView(controller)
  private val distributionsView = DistributionsView(controller)

  private val statusLabel = new Label:
    text = controller.statusMessage.value
    styleClass += "status-bar"

  controller.statusMessage.onChange { (_, _, newMessage) => statusLabel.text = newMessage }

  val root: BorderPane = new BorderPane:
    left = navigation
    center = dashboardView.root
    bottom = statusLabel

  dashboardView.refresh()

  private def navigation: VBox = new VBox:
    minWidth = 225
    padding = Insets(24, 16, 20, 16)
    spacing = 10
    styleClass += "sidebar"
    children = Seq(
      new Label("COMMUNITY AID"):
        styleClass += "brand-title",
      new Label("Eligibility & Distribution"):
        styleClass += "brand-subtitle",
      new Separator,
      navigationButton("Dashboard") {
        dashboardView.refresh()
        root.center = dashboardView.root
      },
      navigationButton("Beneficiaries") {
        root.center = beneficiariesView.root
      },
      navigationButton("Eligibility") {
        eligibilityView.refresh()
        root.center = eligibilityView.root
      },
      navigationButton("Distributions") {
        distributionsView.refresh()
        root.center = distributionsView.root
      },
      new VBox:
        VBox.setVgrow(this, Priority.Always),
      new Label("SDG 1 • No Poverty"):
        styleClass += "sidebar-footer"
    )

  private def navigationButton(buttonText: String)(action: => Unit): Button = new Button(buttonText):
    maxWidth = Double.MaxValue
    styleClass += "nav-button"
    onAction = handle { action }
