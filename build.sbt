name := """pi-fun"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

