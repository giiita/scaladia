name := "scaladia"

version := "0.1"

scalaVersion := "2.12.4"

crossScalaVersions in ThisBuild := Seq("2.10.7", "2.11.12", "2.12.4")

organization in ThisBuild := "com.giitan"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.12.4",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)