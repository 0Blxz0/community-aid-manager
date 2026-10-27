package org.communityaid

import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import org.communityaid.controller.AppController
import org.communityaid.persistence.RepositoryFactory
import org.communityaid.ui.MainView

object CommunityAidApp extends JFXApp3:
  override def start(): Unit =
    val controller = AppController(
      RepositoryFactory.beneficiaryRepository,
      RepositoryFactory.aidRepository
    )
    val mainView = MainView(controller)

    stage = new JFXApp3.PrimaryStage:
      title = "Community Aid Eligibility & Distribution Manager"
      minWidth = 1040
      minHeight = 680
      scene = new Scene(1180, 760):
        root = mainView.root
        Option(getClass.getResource("/styles.css")).foreach(resource => stylesheets += resource.toExternalForm)
