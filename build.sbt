name := "ScalaActorsPlay"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion = "2.4.0"
  val akkaStreamsVersion = "1.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion withJavadoc() withSources(),
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion withJavadoc() withSources(),
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamsVersion withJavadoc() withSources(),
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamsVersion withJavadoc() withSources(),
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamsVersion withJavadoc() withSources(),
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamsVersion withJavadoc() withSources(),
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamsVersion withJavadoc() withSources(),
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0" withJavadoc() withSources(),
    "com.google.code.gson" % "gson" % "2.3.1" withJavadoc() withSources(),
//    "net.liftweb" % "lift-json-ext_2.11" % "2.6-M4",
    "ch.qos.logback" % "logback-classic" % "1.1.3" withJavadoc() withSources(),
    "org.scalatest" %% "scalatest" % "2.2.4" % "test" withJavadoc() withSources(),
    "com.typesafe.akka" %% "akka-testkit" % akkaStreamsVersion % "test" withJavadoc() withSources()
  )
}

fork in run := true