package com.alangeorge.scala.actorplay.avionics

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object Plane {
  case class GiveMeControl()
  case class Controls(controls: ActorRef)
}

object ControlSurfaces {
  case class StickBack(amount: Float)
  case class StickForward(amount: Float)
}

class ControlSurfaces(altimeter: ActorRef) extends Actor {
  import Altimeter._
  import ControlSurfaces._

  override def receive: Receive = {
    case StickBack(amount) => altimeter ! RateChange(amount)
    case StickForward(amount) => altimeter ! RateChange(-1 * amount)
  }
}

class Plane extends Actor with ActorLogging {
  import Altimeter._
  import EventSource._
  import Plane._
  import Pilots._

  val cfgstr = "com.alangeorge.scala.actorplay.avionics.flightcrew"
  val config = context.system.settings.config
  val altimeter = context.actorOf(Props(Altimeter()), "Altimeter")
  val controls = context.actorOf(Props(new ControlSurfaces(altimeter)), "ControlSurfaces")
  val pilot = context.actorOf(Props[Pilot], config.getString(s"$cfgstr.pilotName"))
  val copilot = context.actorOf(Props[Copilot], config.getString(s"$cfgstr.copilotName"))
  val autopilot = context.actorOf(Props[Autopilot], "AutoPilot")
  val flightAttendant = context.actorOf(Props[LeadFlightAttendant], config.getString(s"$cfgstr.leadAttendantName"))

  override def receive: Receive = {
    case GiveMeControl =>
      log info "Plane giving control"
      sender ! Controls(controls)
    case AltimeterUpdate(altitude) =>
      log info s"altitude = $altitude"
  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    altimeter ! RegisterListener(self)
    List(pilot, copilot) foreach { _ ! ReadyToGo }
  }
}
