package com.alangeorge.scala.actorplay.avionics

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}

object TestFlightAttendant {
  def apply() = new FlightAttendant with AttendantResponsiveness { val maxResponseTimeMS = 1 }
}

class FlightAttendantSpec extends
TestKit(ActorSystem("FlightAttendantSpec", ConfigFactory.parseString("akka.scheduler.tick-duration = 1ms")))
with ImplicitSender
with WordSpecLike
with Matchers {

  import FlightAttendant._

  "FlightAttandant" should {
    "get a drink when asked" in {
      val a = system.actorOf(Props(TestFlightAttendant()))
      a ! GetDrink("Soda")
      expectMsg(Drink("Soda"))
    }
  }
}