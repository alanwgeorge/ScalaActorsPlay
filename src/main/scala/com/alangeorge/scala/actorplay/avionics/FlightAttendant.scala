package com.alangeorge.scala.actorplay.avionics

import akka.actor._
import scala.concurrent.duration._

trait AttendantResponsiveness {
  val maxResponseTimeMS: Int
  def responseDuration = scala.util.Random.nextInt(maxResponseTimeMS).millis
}

object FlightAttendant {
  case class GetDrink(drinkname: String)
  case class Drink(drinkname: String)
  def apply() = new FlightAttendant with AttendantResponsiveness { val maxResponseTimeMS = 30000 }
}

class FlightAttendant extends Actor with ActorLogging {
  this: AttendantResponsiveness =>
  import FlightAttendant._

  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case GetDrink(drinkname) => log.debug(s"FlightAttendant GetDrink({})", drinkname); context.system.scheduler.scheduleOnce(responseDuration, sender(), Drink(drinkname))
  }
}

