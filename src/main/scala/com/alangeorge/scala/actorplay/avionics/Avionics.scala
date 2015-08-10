package com.alangeorge.scala.actorplay.avionics

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Avionics {
  implicit val timeout = Timeout(5 seconds)
  val system = ActorSystem("PlaneSimulation")
  val plane = system.actorOf(Props[Plane], name = "Plane")

  def main(args: Array[String]) {
    val control = Await.result((plane ? Plane.GiveMeControl).mapTo[ActorRef], 5 seconds)
    system.scheduler.scheduleOnce(200.millis) {
      control ! ControlSurfaces.StickBack(1f)
    }
    system.scheduler.scheduleOnce(3.seconds) {
      control ! ControlSurfaces.StickBack(0f)
    }
    system.scheduler.scheduleOnce(5.seconds) {
      control ! ControlSurfaces.StickBack(0.5f)
    }
    system.scheduler.scheduleOnce(10.seconds) {
      control ! ControlSurfaces.StickBack(0f)
    }
    system.scheduler.scheduleOnce(15.seconds) {
      system.shutdown()
    }
  }
}
