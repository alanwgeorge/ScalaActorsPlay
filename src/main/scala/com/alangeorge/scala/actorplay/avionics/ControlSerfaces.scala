package com.alangeorge.scala.actorplay.avionics

import akka.actor.{Actor, ActorRef}

object ControlSerfaces {
  case class StickBack(amount: Float)
  case class StickForward(amount: Float)
}

class ControlSerfaces(altimeter: ActorRef) extends Actor {
  import Altimeter._
  import ControlSerfaces._

  override def receive: Receive = {
    case StickBack(amount) => altimeter ! RateChange(amount)
    case StickForward(amount) => altimeter ! RateChange(-1 * amount)
  }
}
