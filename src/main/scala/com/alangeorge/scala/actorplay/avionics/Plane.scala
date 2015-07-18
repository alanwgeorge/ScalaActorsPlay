package com.alangeorge.scala.actorplay.avionics

import akka.actor.{Actor, ActorLogging, Props}
import com.alangeorge.scala.actorplay.avionics.Altimeter.AltimeterUpdate
import com.alangeorge.scala.actorplay.avionics.EventSource.RegisterListener

object Plane {
  case class GiveMeControl()
}

class Plane extends Actor with ActorLogging {
  import Plane._

  val altimeter = context.actorOf(Props(Altimeter()), "Altimeter")
  val controls = context.actorOf(Props(new ControlSerfaces(altimeter)), "ControlSurfaces")

  override def receive: Receive = {
    case GiveMeControl => log info "Plane giving control"
      sender ! controls
    case AltimeterUpdate(altitude) => log info s"altitude = $altitude"
  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    altimeter ! RegisterListener(self)
  }
}
