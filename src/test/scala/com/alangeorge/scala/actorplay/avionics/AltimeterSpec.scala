package com.alangeorge.scala.actorplay.avionics

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestLatch}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

import scala.concurrent.Await

class AltimeterSpec extends TestKit(ActorSystem("AltimeterSpec")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  import Altimeter._

  override protected def afterAll(): Unit = system.shutdown()

  class Helper {
    object EventSourceSpy { val latch = TestLatch(1) }
    trait EventSourceSpy extends EventSource {
      override def sendEvent[T](event: T): Unit = EventSourceSpy.latch.countDown()
      override def eventSourceReceive: Receive = Actor.emptyBehavior
    }
    def slicedAltimeter: Altimeter = new Altimeter with EventSourceSpy
    def actor(): (ActorRef, Altimeter) = {
      val a: TestActorRef[Altimeter] = TestActorRef[Altimeter](Props(slicedAltimeter))
      (a, a.underlyingActor)
    }
  }

  "Altimeter" should {
    "record rate of climb changes" in new Helper {
      val (_, real) = actor()
      real.receive(RateChange(1f))
      real.rateOfClimb should be (real.maxRateOfClimb)
    }
    "keep rate of climb within bounds" in new Helper {
      val (_, real) = actor()
      real.receive(RateChange(2f))
      real.rateOfClimb should be (real.maxRateOfClimb)
    }
    "calculate altitude changes" in new Helper {
      val ref = system.actorOf(Props(Altimeter()))
      ref ! EventSource.RegisterListener(testActor)
      ref ! RateChange(1f)
      fishForMessage() {
        case AltimeterUpdate(altitude) if altitude == 0f => false
        case AltimeterUpdate(altitude) => true
      }
    }
    "send events" in new Helper {
      val (_, real) = actor()
      Await.ready(EventSourceSpy.latch, 1.second)
      EventSourceSpy.latch.isOpen should be (true)
    }
  }
}
