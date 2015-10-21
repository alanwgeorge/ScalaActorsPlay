package com.alangeorge.scala.actorplay.session

import akka.actor.{Props, ActorSystem}
import akka.pattern._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps

object Play extends App {
  implicit val system = ActorSystem("session-system")
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(3 seconds)

  val route = {
    logRequestResult("session") {
      path("session") {
        get {
          complete {
            val requestSender = system.actorOf(Props[RequestSender])
            val requestFuture = requestSender ? Request("/stop", "GET")
            "helloworld"
          }
        }
      } ~ path("stop") {
        get {
          complete {
            system.terminate()
            "stopped"
          }
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  import system.dispatcher
  val addressFuture = bindingFuture.map(_.localAddress)
  addressFuture.onComplete(println(_))
}
