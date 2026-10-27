ThisBuild / scalaVersion := "3.3.6"
ThisBuild / organization := "edu.sunway.prg2104"
ThisBuild / version := "1.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "community-aid-manager",
    Compile / mainClass := Some("org.communityaid.CommunityAidApp"),
    Compile / run / fork := true,
    Compile / run / javaOptions += "-Dcommunityaid.database.path=data/community-aid.db",
    Test / fork := true,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Wunused:all"
    ),
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32",
      "org.xerial" % "sqlite-jdbc" % "3.53.2.0",
      "org.slf4j" % "slf4j-nop" % "2.0.17",
      "org.scalameta" %% "munit" % "1.0.2" % Test
    )
  )
