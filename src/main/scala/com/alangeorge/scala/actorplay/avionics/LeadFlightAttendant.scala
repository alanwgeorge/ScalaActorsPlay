package com.alangeorge.scala.actorplay.avionics

import akka.actor.{Actor, ActorRef, Props}

trait AttendantCreationPolicy {
  val numberOfAttendants: Int = 8
  def createAttendant: Actor = FlightAttendant()
}

trait LeadFlightAttendantProvider {
  def newLeadFlightAttendant = LeadFlightAttendant()
}

object LeadFlightAttendant {
  case class GetFlightAttendant()
  case class Attendant(a: ActorRef)
  def apply() = new LeadFlightAttendant with AttendantCreationPolicy
}

class LeadFlightAttendant extends Actor {
  this: AttendantCreationPolicy =>

  import LeadFlightAttendant._

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    import scala.collection.JavaConversions._
    val attendantNames = context.system.settings.config.getStringList("com.alangeorge.scala.actorplay.avionics.flightcrew.attendantNames")
    attendantNames take numberOfAttendants foreach {
      name => context.actorOf(Props(createAttendant), name)
    }
  }

  def randomAttendant(): ActorRef = {
    context.children.take(scala.util.Random.nextInt(numberOfAttendants)).last
  }

  override def receive: Actor.Receive = {
    case GetFlightAttendant => sender ! randomAttendant()
    case m => randomAttendant() forward m
  }
}

object FlightAttendantPathChecker extends App {
  val system = akka.actor.ActorSystem("PlaneSimulation")
  val lead = system.actorOf(Props(new LeadFlightAttendant with AttendantCreationPolicy),
    system.settings.config.getString("com.alangeorge.scala.actorplay.avionics.flightcrew.leadAttendantName"))
  Thread.sleep(2000)
  system.terminate()
}
