name := "ScalaActorsPlay"

version := "1.0"

scalaVersion := "2.11.7"



libraryDependencies ++= {
  val akkaVersion = "2.3.12"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion withJavadoc() withSources(),
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion withJavadoc() withSources(),
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0" withJavadoc() withSources(),
    "com.google.code.gson" % "gson" % "2.3.1" withJavadoc() withSources(),
    "net.liftweb" % "lift-json-ext_2.11" % "2.6-M4" withJavadoc() withSources(),
    "org.scalatest" %% "scalatest" % "2.2.4" % "test" withJavadoc() withSources(),
    "com.typesafe.akka" %% "akka-testkit" % "2.3.12" withJavadoc() withSources(),
    "ch.qos.logback" % "logback-classic" % "1.1.3" withJavadoc() withSources()
  )
}

fork in run := true