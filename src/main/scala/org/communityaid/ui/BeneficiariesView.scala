package org.communityaid.ui

import scala.annotation.nowarn
import scalafx.Includes.*
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, ComboBox, Label, TableColumn, TableView, TextField}
import scalafx.scene.layout.{GridPane, HBox, Priority, VBox}
import org.communityaid.controller.AppController
import org.communityaid.model.{Beneficiary, EmploymentStatus}

@nowarn("cat=deprecation")
final class BeneficiariesView(controller: AppController):
  private val editingIdentifier = StringProperty("")
  private val beneficiaryType = new ComboBox[String](ObservableBuffer("Household", "Individual"))
  private val fullName = new TextField:
    promptText = "e.g. Amina Rahman"
  private val monthlyIncome = new TextField:
    promptText = "e.g. 1800.00"
  private val dependantCount = new TextField:
    promptText = "0 to 20"
  private val employment = new ComboBox[EmploymentStatus](ObservableBuffer.from(EmploymentStatus.values))
  private val location = new TextField:
    promptText = "e.g. Subang Jaya"

  private val table = new TableView[Beneficiary]:
    items = controller.beneficiaries
    columnResizePolicy = TableView.ConstrainedResizePolicy
    placeholder = new Label("No beneficiaries registered.")

  beneficiaryType.selectionModel().selectFirst()
  employment.selectionModel().selectFirst()

  table.columns ++= List(
    textColumn("ID", 90, _.id),
    textColumn("Name", 190, _.fullName),
    textColumn("Type", 100, _.beneficiaryType),
    textColumn("Income", 100, beneficiary => f"RM ${beneficiary.monthlyIncome}%.2f"),
    textColumn("Dependants", 85, _.dependants.toString),
    textColumn("Status", 105, _.eligibilityStatus.label),
    textColumn("Location", 150, _.location)
  )

  private val saveButton = new Button("Add beneficiary"):
    defaultButton = true
    styleClass += "primary-button"
    onAction = handle { saveForm() }

  private val loadButton = new Button("Edit selected"):
    onAction = handle {
      selectedBeneficiary match
        case Some(beneficiary) => populateForm(beneficiary)
        case None => UiSupport.showError("Select a beneficiary in the table first.")
    }

  private val clearButton = new Button("Clear form"):
    onAction = handle { clearForm() }

  private val deleteButton = new Button("Delete selected"):
    styleClass += "danger-button"
    onAction = handle {
      selectedBeneficiary match
        case Some(beneficiary) =>
          controller.deleteBeneficiary(beneficiary.id) match
            case Right(_) => clearForm()
            case Left(message) => UiSupport.showError(message)
        case None => UiSupport.showError("Select a beneficiary in the table first.")
    }

  private val form = new GridPane:
    hgap = 12
    vgap = 12
    styleClass += "content-card"
    add(new Label("Applicant type"), 0, 0)
    add(beneficiaryType, 1, 0)
    add(new Label("Full name"), 0, 1)
    add(fullName, 1, 1)
    add(new Label("Monthly income (RM)"), 0, 2)
    add(monthlyIncome, 1, 2)
    add(new Label("Dependants"), 2, 0)
    add(dependantCount, 3, 0)
    add(new Label("Employment"), 2, 1)
    add(employment, 3, 1)
    add(new Label("Location"), 2, 2)
    add(location, 3, 2)

  GridPane.setHgrow(fullName, Priority.Always)
  GridPane.setHgrow(location, Priority.Always)

  val root: VBox = new VBox:
    spacing = 16
    padding = Insets(24)
    VBox.setVgrow(table, Priority.Always)
    children = Seq(
      UiSupport.pageTitle("Beneficiaries"),
      new Label("Register applicants, correct their details and review eligibility at a glance."):
        styleClass += "page-subtitle",
      form,
      new HBox:
        spacing = 10
        children = Seq(saveButton, loadButton, clearButton, deleteButton),
      table
    )

  private def saveForm(): Unit =
    val selectedEmployment = Option(employment.value.value).getOrElse(EmploymentStatus.Unemployed)
    val result =
      if editingIdentifier.value.isEmpty then
        controller.addBeneficiary(
          beneficiaryType.value.value,
          fullName.text.value,
          monthlyIncome.text.value,
          dependantCount.text.value,
          selectedEmployment,
          location.text.value
        )
      else
        controller.updateBeneficiary(
          editingIdentifier.value,
          beneficiaryType.value.value,
          fullName.text.value,
          monthlyIncome.text.value,
          dependantCount.text.value,
          selectedEmployment,
          location.text.value
        )

    result match
      case Right(_) => clearForm()
      case Left(message) => UiSupport.showError(message)

  private def populateForm(beneficiary: Beneficiary): Unit =
    editingIdentifier.value = beneficiary.id
    beneficiaryType.value.value = beneficiary.beneficiaryType
    fullName.text.value = beneficiary.fullName
    monthlyIncome.text.value = beneficiary.monthlyIncome.toString
    dependantCount.text.value = beneficiary.dependants.toString
    employment.value.value = beneficiary.employmentStatus
    location.text.value = beneficiary.location
    saveButton.text = "Update beneficiary"

  private def clearForm(): Unit =
    editingIdentifier.value = ""
    fullName.clear()
    monthlyIncome.clear()
    dependantCount.text = "0"
    location.clear()
    beneficiaryType.selectionModel().selectFirst()
    employment.selectionModel().selectFirst()
    table.selectionModel().clearSelection()
    saveButton.text = "Add beneficiary"

  private def selectedBeneficiary: Option[Beneficiary] =
    Option(table.selectionModel().selectedItem.value)

  private def textColumn(
      heading: String,
      requestedWidth: Double,
      valueFor: Beneficiary => String
  ): TableColumn[Beneficiary, String] = new TableColumn[Beneficiary, String]:
    text = heading
    prefWidth = requestedWidth
    cellValueFactory = cellData => StringProperty(valueFor(cellData.value))
