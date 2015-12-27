package com.alangeorge.scala.actorplay

import akka.actor._
import akka.pattern.{ask, gracefulStop}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.google.gson.Gson

//import net.liftweb.json._
//import net.liftweb.json.Serialization.write
//import net.liftweb.json.JsonDSL._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}

// Hello
class HelloActor(name: String) extends Actor {
  override def receive: Receive = {
    case "hello" => println(s"hello from $name")
    case _ =>
      println(s"huh?, said $name")
  }
}

object HelloMain extends App {
  val system = ActorSystem("HelloSystem")
  val helloActor = system.actorOf(Props(new HelloActor("Alan")), name = "helloactor")

  helloActor ! "hello"
  helloActor ! "buenos dias"

  system.terminate()
}

// Ping Pong
case object PingMessage
case object PongMessage
case object StartMessage
case object StopMessage

class Ping(pong: ActorRef) extends Actor {
  println("Ping thread:" + Thread.currentThread().getName)
  var count = 0
  def incrementAndPrint() {count += 1; println("ping")}
  override def receive: Actor.Receive = {
    case StartMessage =>
      incrementAndPrint()
      pong ! PingMessage
    case PongMessage =>
      incrementAndPrint()
      if (count > 99) {
        sender ! StopMessage
        println("ping stopped")
        context.stop(self)
      } else {
        sender ! PingMessage
      }
    case _ => println("Ping got something unexpected.")
  }
}

class Pong extends Actor {
  println("Pong thread:" + Thread.currentThread().getName)
  override def receive: Actor.Receive = {
    case PingMessage =>
      println("pong")
      sender ! PongMessage
    case StopMessage =>
      println("pong stopped")
      context.stop(self)
    case _ => println("Pong got something unexpected.")
  }
}

object PingPongTest extends App {
  println("PingPongTest thread:" + Thread.currentThread().getName)
  val system = ActorSystem("PingPongSystem")
  val pong = system.actorOf(Props[Pong], name = "pong")
  val ping = system.actorOf(Props(new Ping(pong)), name = "ping")

  ping ! StartMessage
}


// Who killed Kenny
class Kenny extends Actor {
  println("entered the Kenny constructor")
  println("Kenny thread:" + Thread.currentThread().getName)
  override def preStart() { println("kenny: preStart") }
  override def postStop() { println("kenny: postStop") }
  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("kenny: preRestart")
    println(s"  MESSAGE: ${message.getOrElse("")}")
    println(s"  REASON: ${reason.getMessage}")
    super.preRestart(reason, message)
  }
  override def postRestart(reason: Throwable) {
    println("kenny: postRestart")
    println(s"  REASON: ${reason.getMessage}")
    super.postRestart(reason)
  }
  def receive = {
    case ForceRestart => throw new Exception("Boom!")
    case _ => println("Kenny received a message")
  }
}

case object ForceRestart

object LifecycleDemo extends App {
  println("LifecycleDemo thread:" + Thread.currentThread().getName)
  val system = ActorSystem("LifecycleDemo")
  val kenny = system.actorOf(Props[Kenny], name = "Kenny")

  println("sending kenny a simple String message")
  kenny ! "hello"
  Thread.sleep(1000)

  println("make kenny restart")
  kenny ! ForceRestart
  Thread.sleep(1000)

  println("stopping kenny")
  system.stop(kenny)

  println("shutting down system")
  system.terminate()
}


// Parent Child Actors
case class CreateChild (name: String)
case class Name (name: String)

class Child extends Actor {
  println("child created")
  var name = "No name"
  override def postStop() {
    println(s"D'oh! They killed me ($name): ${self.path}")
  }
  def receive = {
    case Name(n) => this.name = n
    case _ => println(s"Child $name got message")
  }
}

class Parent extends Actor {
  def receive = {
    case CreateChild(name) =>
      // Parent creates a new Child here
      println(s"Parent about to create Child ($name) ...")
      val child = context.actorOf(Props[Child], name = s"$name")
      child ! Name(name)
    case _ => println(s"Parent got some other message.")
  }
}

object ParentChildDemo extends App {
  val actorSystem = ActorSystem("ParentChildTest")
  val parent = actorSystem.actorOf(Props[Parent], name = "Parent")

  // send messages to Parent to create to child actors
  parent ! CreateChild("Jonathan")
  parent ! CreateChild("Jordan")
  Thread.sleep(500)

  // lookup Jonathan, then kill it
  println("Sending Jonathan a PoisonPill ...")
  val jonathan = actorSystem.actorSelection("/user/Parent/Jonathan")
  jonathan ! PoisonPill
  println("jonathan was killed")

  Thread.sleep(5000)
  actorSystem.terminate()
}


// GracefulStop
class TestActor extends Actor {
  def receive = {
    case _ => println("TestActor got message")
  }
  override def postStop() { println("TestActor: postStop") }
}

object GracefulStopTest extends App {
  val system = ActorSystem("GracefulStopTest")
  val testActor = system.actorOf(Props[TestActor], name = "TestActor")

  // try to stop the actor gracefully
  try {
    val stopped: Future[Boolean] = gracefulStop(testActor, 2 seconds)
    Await.result(stopped, 3 seconds)
    println("testActor was stopped")
  } catch {
    case e:Exception => e.printStackTrace()
  } finally {
    system.terminate()
  }
}


// Kill
class Number5 extends Actor {
  def receive = {
    case _ => println("Number5 got a message")
  }
  override def preStart() { println("Number5 is alive") }
  override def postStop() { println("Number5::postStop called") }
  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("Number5::preRestart called")
  }
  override def postRestart(reason: Throwable) {
    println("Number5::postRestart called")
  }
}

object KillTest extends App {
  val system = ActorSystem("KillTestSystem")
  val number5 = system.actorOf(Props[Number5], name = "Number5")
  number5 ! "hello"
  // send the Kill message
  number5 ! Kill
  system.terminate()
}


// Death Watch
class Parent2 extends Actor {
  // start Kenny as a child, then keep an eye on it
  val kenny = context.actorOf(Props[Kenny], name = "Kenny")
  context.watch(kenny)

  def receive = {
    case Terminated(k) => println("OMG, they killed Kenny")
    case _ => println("Parent received a message")
  }
}

object DeathWatchTest extends App {
  // create the ActorSystem instance
  val system = ActorSystem("DeathWatchTest")

  // create the Parent that will create Kenny
  val parent = system.actorOf(Props[Parent2], name = "Parent2")

  // lookup kenny, then kill it
  val kenny = system.actorSelection("/user/Parent2/Kenny")
  kenny ! ForceRestart
  kenny ! "hello?"

  Thread.sleep(5000)
  println("calling system.shutdown")
  system.terminate()
}

// Futures1
object Futures1 extends App {
  val f = Future {
    sleep(500)
    1 + 1
  }

  // this is blocking (blocking is bad)
  val result = Await.result(f, 1 second)
  println(result)

  sleep(1000)
}


// Futures2
object Futures2 extends App {
  println("starting calculation ..." + " " + Thread.currentThread().getName)
  val f = Future {
    sleep(Random.nextInt(500))
    if (Random.nextInt(500) > 250) throw new Exception("Yikes!") else 42
  }

  println("before onComplete" + " " + Thread.currentThread().getName)
  f.onComplete {
    case Success(value) => println(s"Got the callback, meaning = $value" + " " + Thread.currentThread().getName)
    case Failure(e) => e.printStackTrace()
  }

  // do the rest of your work
  println("A ..." + " " + Thread.currentThread().getName); sleep(100)
  println("B ..." + " " + Thread.currentThread().getName); sleep(100)
  println("C ..." + " " + Thread.currentThread().getName); sleep(100)
  println("D ..." + " " + Thread.currentThread().getName); sleep(100)
  println("E ..." + " " + Thread.currentThread().getName); sleep(100)
  println("F ..." + " " + Thread.currentThread().getName); sleep(100)

  sleep(2000)
}

object Futures3 extends App {
  def longRunningComputation(i: Int): Future[Int] = Future {
    sleep(100)
    i + 1
  }

  // this does not block
  longRunningComputation(11).onComplete {
    case Success(result) => println(s"result = $result")
    case Failure(e) => e.printStackTrace()
  }

  // keep the jvm from shutting down
  sleep(1000)
}


// RunningMultipleCalcs
object Cloud {
  def runAlgorithm(i: Int): Future[Int] = Future {
    sleep(Random.nextInt(500))
    val result = i + 10
    println(s"returning result from cloud: $result" + " " + Thread.currentThread().getName)
    result
  }
}

object RunningMultipleCalcs extends App {
  println("starting futures")
  val result1 = Cloud.runAlgorithm(10)
  val result2 = Cloud.runAlgorithm(20)
  val result3 = Cloud.runAlgorithm(30)

  println("before for-comprehension")
  val result = for {
         r1 <- result1
         r2 <- result2
         r3 <- result3
  } yield r1 + r2 + r3

  println("before onSuccess")
  result onSuccess {
    case r => println(s"total = $r" + " " + Thread.currentThread().getName)
  }

  println("before sleep at the end")
  sleep(2000)  // keep the jvm alive
}


// AskTest
case object AskNameMessage

class TestActor2 extends Actor {
  def receive = {
    case AskNameMessage => // respond to the 'ask' request
      sender ! "Fred"
    case _ => println("that was unexpected")
  }
}

object AskTest extends App {
  // create the system and actor
  val system = ActorSystem("AskTestSystem")
  val myActor = system.actorOf(Props[TestActor2], name = "myActor")

  // (1) this is one way to "ask" another actor for information
  implicit val timeout = Timeout(5 seconds)
  val future = myActor ? AskNameMessage
  val result = Await.result(future, timeout.duration).asInstanceOf[String]
  println(result)
  // (2) a slightly different way to ask another actor for information
  val future2: Future[String] = ask(myActor, AskNameMessage).mapTo[String]
  val result2 = Await.result(future2, 1 second)
  println(result2)

  system.terminate()
}


// BecomeHulkExample
case object ActNormalMessage
case object TryToFindSolution
case object BadGuysMakeMeAngry

class DavidBanner extends Actor {
  import context._

  def angryState: Receive = {
    case ActNormalMessage =>
      println("Phew, I'm back to being David.")
      become(normalState)
  }

  def normalState: Receive = {
    case TryToFindSolution =>
      println("Looking for solution to my problem ...")
    case BadGuysMakeMeAngry =>
      println("I'm getting angry...")
      become(angryState)
  }

  def receive = {
    case BadGuysMakeMeAngry => become(angryState)
    case ActNormalMessage => become(normalState)
  }
}

object BecomeHulkExample extends App {
  val system = ActorSystem("BecomeHulkExample")
  val davidBanner = system.actorOf(Props[DavidBanner], name = "DavidBanner")
  davidBanner ! ActNormalMessage // init to normalState
  davidBanner ! TryToFindSolution
  davidBanner ! BadGuysMakeMeAngry
  Thread.sleep(1000)
  davidBanner ! ActNormalMessage
  system.terminate()
}


// Gson
case class Person(name: String, address: Address2)
case class Address2(city: String, state: String)

object GsonTest extends App {
  val p = Person("Alvin Alexander", Address2("Talkeetna", "AK"))
  // create a JSON string from the Person, then print it
  val gson = new Gson
  val jsonString = gson.toJson(p)
  println(jsonString)

  val p2 = gson.fromJson(jsonString, classOf[Person])
  println(p2)
}

object SprayJsonTest extends App {
  import spray.json._
  import DefaultJsonProtocol._

  val address = Address2("san francisco", "ca")
  val person = Person("alan george", address)

  implicit val personFormat= jsonFormat2(Address2)
  implicit val addressFormat = jsonFormat2(Person)

  println(person.toJson.prettyPrint)
}


object FlowTest extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val source: Source[Int, Unit] = Source(1 to 10)
  val sink: Sink[Int, Future[Int]] = Sink.fold[Int,Int](0)(_ + _)
  val sum: Future[Int] = source.runWith(sink)

  val result: Int = Await.result(sum, 1 second)
  println(result)
}

//// Lift Web
//object LiftJsonTest extends App {
//  val p = Person("Alvin Alexander", Address2("Talkeetna", "AK"))
//
//  // create a JSON string from the Person, then print it
//  implicit val formats = DefaultFormats
//  val jsonString = write(p)
//  println(jsonString)
//}
//
//case class Person2(name: String, address: Address2) {
//  var friends = List[Person2]()
//}
//
//object LiftJsonListsVersion1 extends App {
//  //import net.liftweb.json.JsonParser._
//  val formats = DefaultFormats
//
//  val merc = Person2("Mercedes", Address2("Somewhere", "KY"))
//  val mel = Person2("Mel", Address2("Lake Zurich", "IL"))
//  val friends = List(merc, mel)
//  val p = Person2("Alvin Alexander", Address2("Talkeetna", "AK"))
//  p.friends = friends
//
//  // define the json output
//  val json =
//    "person" ->
//      ("name" -> p.name) ~
//        ("address" ->
//          ("city" -> p.address.city) ~
//            ("state" -> p.address.state)) ~
//        ("friends" ->
//          friends.map { f =>
//            ("name" -> f.name) ~
//              ("address" ->
//                ("city" -> f.address.city) ~
//                  ("state" -> f.address.state))
//          })
//
//  println(pretty(render(json)))
//}