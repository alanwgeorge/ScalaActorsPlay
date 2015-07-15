name := "ScalaActorsPlay"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.12"
libraryDependencies += "com.google.code.gson" % "gson" % "2.3.1"
libraryDependencies += "net.liftweb" % "lift-json-ext_2.11" % "2.6-M4"

fork in run := true