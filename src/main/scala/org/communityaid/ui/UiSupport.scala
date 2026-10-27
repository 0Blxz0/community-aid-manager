package org.communityaid.ui

import scalafx.scene.control.{Alert, Label}

object UiSupport:
  def pageTitle(textValue: String): Label = new Label:
    text = textValue
    styleClass += "page-title"

  def sectionTitle(textValue: String): Label = new Label:
    text = textValue
    styleClass += "section-title"

  def showError(message: String): Unit =
    new Alert(Alert.AlertType.Error):
      title = "Please check the form"
      headerText = "The action could not be completed"
      contentText = message
    .showAndWait()

  def showInformation(header: String, message: String): Unit =
    new Alert(Alert.AlertType.Information):
      title = "Community Aid Manager"
      headerText = header
      contentText = message
    .showAndWait()
