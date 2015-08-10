package com.alangeorge.scala.actorplay.avionics

import akka.actor.{ActorLogging, ActorNotFound, Actor, ActorRef}

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

  override def receive: Actor.Receive = {
    case ReadyToGo =>
      context.parent ! GiveMeControl
//      copilot = context.actorFor("../" + copilotName)
      context.actorSelection("../" + copilotName).resolveOne().onComplete {
        case Success(cp) => copilot = cp
        case Failure(e) => throw e
      }
      autopilot = context.actorFor("../Autopilot")
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

  override def receive: Actor.Receive = {
    case ReadyToGo =>
      pilot = context.actorFor("../" + pilotName)
      autopilot = context.actorFor("../Autopilot")
  }
}

class Autopilot extends Actor with ActorLogging {
  override def receive: Actor.Receive = {
    case m => log info s"Autopilot got a message: $m"
  }
}