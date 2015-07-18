package com.alangeorge.scala.actorplay.avionics

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class TestEventSource extends Actor with ProductionEventSource {
  override def receive: Receive = eventSourceReceive
}

class EventSourceSpec extends TestKit(ActorSystem("EventSourceSpec")) with WordSpecLike with Matchers with BeforeAndAfterAll {
  import EventSource._

  override protected def afterAll(): Unit = system.shutdown()

  "EventSource" should {
    "allow us to register a listener" in {
      val real = TestActorRef[TestEventSource].underlyingActor
      real.receive(RegisterListener(testActor))
      real.listeners should contain (testActor)
    }
    "allow us to unregister a listener" in {
      val real = TestActorRef[TestEventSource].underlyingActor
      real.receive(RegisterListener(testActor))
      real.receive(UnregisterListener(testActor))
      real.listeners.size should be (0)
    }
    "send a evetn to our test actor" in {
      val test1 = TestActorRef[TestEventSource]
      test1 ! RegisterListener(testActor)
      test1.underlyingActor.sendEvent("Fibonacci")
      expectMsg("Fibonacci")
    }
  }
}
