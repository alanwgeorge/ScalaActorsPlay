name := "ScalaActorsPlay"

version := "1.0"

scalaVersion := "2.11.7"



libraryDependencies ++= {
  val akkaVersion = "2.3.12"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.google.code.gson" % "gson" % "2.3.1",
    "net.liftweb" % "lift-json-ext_2.11" % "2.6-M4",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.12",
    "ch.qos.logback" % "logback-classic" % "1.1.3"
  )
}

fork in run := true