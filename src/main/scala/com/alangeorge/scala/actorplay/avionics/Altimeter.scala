package com.alangeorge.scala.actorplay.avionics

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.duration._

object Altimeter {
  case class RateChange(amount: Float)
  case class AltimeterUpdate(altitude: Double)
}

class Altimeter extends Actor with ActorLogging with EventSource {
  import Altimeter._
  implicit val ec = context.dispatcher

  val ceiling = 43000
  val maxRateOfClimb = 5000
  var rateOfClimb = 0f
  var altitude = 0d
  val ticker = context.system.scheduler.schedule(100.millis, 100.millis, self, Tick)
  case object Tick
  var lastTick = System.currentTimeMillis

  override def receive: Receive = {
    eventSourceReceive orElse altimeterReceive
  }

  def altimeterReceive: Receive = {
    case RateChange(amount) =>
      rateOfClimb = amount.min(1.0f).max(-1.0f) * maxRateOfClimb
      log info s"Altimeter changed rate of climb to $rateOfClimb."
    case Tick =>
      val tick = System.currentTimeMillis
      altitude = altitude + ((tick - lastTick) / 60000.0) * rateOfClimb
      lastTick = tick
      sendEvent(AltimeterUpdate(altitude))
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {log info "stopping the altimeter"; ticker.cancel()}
}
