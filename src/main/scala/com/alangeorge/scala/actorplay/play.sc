import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._


val f = Future {
  println("HelloWorld!")
  Thread.sleep(5)
  1
}

f.onComplete {
  case Success(result) => println(s"result = $result")
  case Failure(e) => e.printStackTrace()
}

Await.result(f, 10 seconds)

f.isCompleted
