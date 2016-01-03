package com.alangeorge.scala.actorplay

import java.util.concurrent.LinkedBlockingDeque

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.language.{implicitConversions, postfixOps}
import scala.util.Random

trait Animal {
  def makeSound: String
}

class Dog extends Animal {
  override def makeSound: String = "Woof"
}

class Cat extends Animal {
  override def makeSound: String = "Meow"
}

class AnimalApi extends LazyLogging {
  import AnimalApi._
  def execute: Future[Animal] = {
    val promise = Promise[Animal]
    val f = Future[Int] {
      val i = Random.nextInt(MAX_SLEEP)
      logger.info(s"sleeping $i")
      sleep(i)
      i
    }
    f.map { i => if (i % 10 == 0) promise.success(new Cat) else promise.success(new Dog)}
    promise.future
  }
}
object AnimalApi {
  val MAX_SLEEP: Duration = 5 seconds

  implicit def durationToInt(d: Duration): Int = d.toMillis.toInt
}

trait FishingQueue[T] {
  private val queue = new LinkedBlockingDeque[T]()

  def put(t: T): Boolean = queue.offerFirst(t)

  def fishForMessage(max: Duration)(pf: PartialFunction[T, Boolean]): T = {
    val end = now + max

    def recv: T = {
      val o = receiveOne(end - now)
      assert(o != null, "timeout (" + max + ") during fishForMessage")
      assert(pf.isDefinedAt(o), "fishForMessage() found unexpected message " + o)
      if (pf(o)) o else recv
    }
    recv
  }

  private def receiveOne(max: Duration): T = {
      if (max == 0.seconds) {
        queue.pollFirst
      } else if (max.isFinite) {
        queue.pollFirst(max.length, max.unit)
      } else {
        queue.takeFirst
      }
  }

  private def now: FiniteDuration = System.nanoTime.nanos
}

object Fishing extends FishingQueue[Option[Animal]] with LazyLogging {
  def main(args: Array[String]) {
    import AnimalApi._

    val api = new AnimalApi

    val someFutures = (0 to 9) map(_ => api.execute)

    for {
      f <- someFutures
    } yield f.onComplete(r => put(r toOption))

    val catsOnly: PartialFunction[Any, Boolean] = {
      case Some(animal) =>
        animal match {
          case c: Cat => true
          case o =>
            logger.info(s"$o did not match")
            false
        }
      case None => false
    }

    val fish = fishForMessage(MAX_SLEEP + (1 second))(catsOnly)

    logger.info(s"caught fish $fish")
  }
}

