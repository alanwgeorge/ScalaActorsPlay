package com.alangeorge.scala.actorplay.avionics

import akka.actor.{Actor, ActorLogging, Props}

object Plane {
  case class GiveMeControl()
}

class Plane extends Actor with ActorLogging {
  import Plane._

  val altimeter = context.actorOf(Props[Altimeter], "Altimeter")
  val controls = context.actorOf(Props(new ControlSerfaces(altimeter)), "ControlSurfaces")

  override def receive: Receive = {
    case GiveMeControl => log info "Plane giving control"
      sender ! controls
  }
}
