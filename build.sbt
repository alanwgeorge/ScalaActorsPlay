name := "ScalaActorsPlay"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "com.google.code.gson" % "gson" % "2.3.1",
  "net.liftweb" % "lift-json-ext_2.11" % "2.6-M4",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.12"
)

fork in run := true