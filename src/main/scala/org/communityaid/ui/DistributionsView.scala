package org.communityaid.ui

import java.time.LocalDate
import scala.annotation.nowarn
import scalafx.Includes.*
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, ComboBox, DatePicker, Label, TableColumn, TableView, TextField}
import scalafx.scene.layout.{GridPane, HBox, Priority, VBox}
import org.communityaid.controller.AppController
import org.communityaid.model.{Aid, Beneficiary, DistributionStatus}

@nowarn("cat=deprecation")
final class DistributionsView(controller: AppController):
  private val beneficiaryChoice = new ComboBox[Beneficiary](controller.beneficiaries):
    promptText = "Choose an eligible beneficiary"
  private val aidType = new ComboBox[String](ObservableBuffer("Cash", "Food"))
  private val value = new TextField:
    promptText = "Amount or estimated value"
  private val description = new TextField:
    promptText = "Required for food aid"
  private val distributionDate = new DatePicker(LocalDate.now())
  private val distributionStatus = new ComboBox[DistributionStatus](ObservableBuffer.from(DistributionStatus.values))
  private val filterChoice = new ComboBox[String](ObservableBuffer("All", "Cash", "Food"))

  private val filteredDistributions = ObservableBuffer.empty[Aid]
  private val table = new TableView[Aid]:
    items = filteredDistributions
    columnResizePolicy = TableView.ConstrainedResizePolicy
    placeholder = new Label("No matching distributions.")

  aidType.selectionModel().selectFirst()
  distributionStatus.selectionModel().select(DistributionStatus.Distributed)
  filterChoice.selectionModel().selectFirst()

  table.columns ++= List(
    textColumn("ID", 85, _.id),
    textColumn("Beneficiary", 190, aid => controller.beneficiaryName(aid.beneficiaryId)),
    textColumn("Type", 80, _.aidType),
    textColumn("Value", 190, _.displayValue),
    textColumn("Date", 110, _.distributedOn.toString),
    textColumn("Status", 105, _.status.label)
  )

  private val recordButton = new Button("Record distribution"):
    defaultButton = true
    styleClass += "primary-button"
    onAction = handle { recordDistribution() }

  private val applyFilterButton = new Button("Apply filter"):
    onAction = handle { refresh() }

  private val form = new GridPane:
    hgap = 12
    vgap = 12
    styleClass += "content-card"
    add(new Label("Beneficiary"), 0, 0)
    add(beneficiaryChoice, 1, 0)
    add(new Label("Aid type"), 0, 1)
    add(aidType, 1, 1)
    add(new Label("Amount / value (RM)"), 0, 2)
    add(value, 1, 2)
    add(new Label("Food description"), 2, 0)
    add(description, 3, 0)
    add(new Label("Distribution date"), 2, 1)
    add(distributionDate, 3, 1)
    add(new Label("Status"), 2, 2)
    add(distributionStatus, 3, 2)

  GridPane.setHgrow(beneficiaryChoice, Priority.Always)
  GridPane.setHgrow(description, Priority.Always)

  val root: VBox = new VBox:
    spacing = 16
    padding = Insets(24)
    VBox.setVgrow(table, Priority.Always)
    children = Seq(
      UiSupport.pageTitle("Aid distributions"),
      new Label("Approve aid for eligible applicants and keep an auditable distribution history."):
        styleClass += "page-subtitle",
      form,
      recordButton,
      new HBox:
        spacing = 10
        children = Seq(UiSupport.sectionTitle("Distribution history"), filterChoice, applyFilterButton),
      table
    )

  def refresh(): Unit =
    if beneficiaryChoice.value.value == null && controller.beneficiaries.nonEmpty then
      beneficiaryChoice.selectionModel().selectFirst()

    val selectedFilter = Option(filterChoice.value.value).getOrElse("All")
    filteredDistributions.clear()
    filteredDistributions ++= controller.aidDistributions.filter { distribution =>
      selectedFilter == "All" || distribution.aidType == selectedFilter
    }

  private def recordDistribution(): Unit =
    val result = for
      beneficiary <- Option(beneficiaryChoice.value.value).toRight("Select a beneficiary.")
      selectedAidType <- Option(aidType.value.value).toRight("Select an aid type.")
      selectedDate <- Option(distributionDate.value.value).toRight("Choose a distribution date.")
      selectedStatus <- Option(distributionStatus.value.value).toRight("Choose a distribution status.")
      saved <- selectedAidType match
        case "Cash" => controller.registerCashAid(beneficiary, value.text.value, selectedDate, selectedStatus)
        case "Food" =>
          controller.registerFoodAid(
            beneficiary,
            description.text.value,
            value.text.value,
            selectedDate,
            selectedStatus
          )
        case unsupported => Left(s"Unsupported aid type: $unsupported")
    yield saved

    result match
      case Right(_) =>
        value.clear()
        description.clear()
        refresh()
      case Left(message) => UiSupport.showError(message)

  private def textColumn(
      heading: String,
      requestedWidth: Double,
      valueFor: Aid => String
  ): TableColumn[Aid, String] = new TableColumn[Aid, String]:
    text = heading
    prefWidth = requestedWidth
    cellValueFactory = cellData => StringProperty(valueFor(cellData.value))
