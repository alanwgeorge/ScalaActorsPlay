package com.alangeorge.scala.futuresplay

import org.scalatest.{Matchers, WordSpec}
import Stream.cons
import scala.language.postfixOps
import scala.util.{Failure, Success}


class MultiExecContextSpec extends WordSpec with Matchers {
  import scala.math.BigInt

  lazy val fibs: Stream[BigInt] = BigInt(0) #:: BigInt(1) #:: fibs.zip(fibs.tail).map { n => n._1 + n._2 }

  "Future" should {
    "calculate fibonacci numbers" in {
      import java.util.concurrent.Executors
      import scala.concurrent.duration._
      import scala.concurrent.{ExecutionContext, Future, Await}

      val execService = Executors.newCachedThreadPool()
      implicit val execContext = ExecutionContext.fromExecutorService(execService)

      val futureFib = Future { fibs.drop(99).head }

      val fib = Await.result(futureFib, 1 second)

      fib should be(BigInt("218922995834555169026"))

      def factorize(num: BigInt): Tuple2[BigInt, Seq[Int]] = {
        import math._
        (num, (1 to floor(sqrt(num.toDouble)).toInt) filter {
          i => num.toInt % i == 0
        })
      }

      val futureFibFactors = Future { fibs.drop(28).head } map { f =>
        factorize(f)
      } andThen {
        case Success(Tuple2(fib2, factors)) => println(s"Factors for $fib2 are ${factors.mkString(", ")}")
        case Failure(e) => println("Somthing went wrong: " + e)
      }

      val r = Await.result(futureFibFactors, 1 second)

      r._1 should be(317811)

      execContext.shutdown()
    }
  }
}
