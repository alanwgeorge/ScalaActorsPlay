package com.alangeorge.scala.actorplay.session

import akka.actor.{ActorSystem, Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.Uri.{Host, Authority}
import akka.http.scaladsl.model.{Uri, HttpResponse, HttpRequest}
import akka.stream.ActorMaterializer

import concurrent.Future

trait RequestSender extends Actor with ActorLogging {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  override def receive: Receive = {
    case r: Request => send(r)
  }

  def send(request: Request) = {
    import system.dispatcher
    val uri: Uri = Uri(request.scheme, Authority(Host(request.host), port = request.port))
    val getRequest = RequestBuilding.Get()
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = uri))
    responseFuture.onComplete(println(_))
  }
}

case class Request(path: String, method: String, scheme: String = "http", host: String = "localhost", port: Int = 8080)
