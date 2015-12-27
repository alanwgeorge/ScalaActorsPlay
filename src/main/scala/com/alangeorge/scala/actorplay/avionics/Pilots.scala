package com.alangeorge.scala.actorplay.avionics

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps

import scala.util.{Failure, Success}

object Pilots {
  case object ReadyToGo
  case object RelinquishControl
}

class Pilot extends Actor with ActorLogging {
  import Pilots._
  import Plane._

  var controls: ActorRef = context.system.deadLetters
  var copilot: ActorRef = context.system.deadLetters
  var autopilot: ActorRef = context.system.deadLetters
  val copilotName = context.system.settings.config.getString("com.alangeorge.scala.actorplay.avionics.flightcrew.copilotName")
  implicit val timeout = Timeout(2 seconds)
  implicit val ec = context.dispatcher

  override def receive: Actor.Receive = {
    case ReadyToGo =>
      context.parent ! GiveMeControl
//      copilot = context.actorFor("../" + copilotName)
      context.actorSelection("../" + copilotName).resolveOne().onComplete {
        case Success(cp) => copilot = cp
        case Failure(e) => throw e
      }
//      autopilot = context.actorFor("../Autopilot")
      context.actorSelection("../Autopilot").resolveOne().onComplete {
        case Success(cp) => autopilot = cp
        case Failure(e) => throw e
      }
    case Controls(controlSurfaces) =>
      controls = controlSurfaces
  }
}

class Copilot extends Actor {
  import Pilots._

  var controls: ActorRef = context.system.deadLetters
  var pilot: ActorRef = context.system.deadLetters
  var autopilot: ActorRef = context.system.deadLetters
  val pilotName = context.system.settings.config.getString("com.alangeorge.scala.actorplay.avionics.flightcrew.pilotName")
  implicit val timeout: Timeout = Timeout(2 seconds)
  implicit val ec = context.dispatcher

  override def receive: Actor.Receive = {
    case ReadyToGo =>
//      pilot = context.actorFor("../" + pilotName)
      context.actorSelection("../" + pilotName).resolveOne().onComplete {
        case Success(cp) => pilot = cp
        case Failure(e) => throw e
      }

//      autopilot = context.actorFor("../Autopilot")
      context.actorSelection("../Autopilot").resolveOne().onComplete {
        case Success(cp) => autopilot = cp
        case Failure(e) => throw e
      }
  }
}

class Autopilot extends Actor with ActorLogging {
  override def receive: Actor.Receive = {
    case m => log info s"Autopilot got a message: $m"
  }
}