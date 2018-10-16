package com.bova.app

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.bova.app.rest.RestApi
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContextExecutor, Future}

object RestApiApp extends RestApi {

  def main(args: Array[String]) {

    implicit val system: ActorSystem = RestApi.system
    implicit val actorMaterializer: ActorMaterializer = RestApi.actorMaterializer
    implicit val ec: ExecutionContextExecutor = RestApi.ec

    val config: Config = RestApi.config
    val host: String = config.getString("http.host") // Gets the host and a port from the configuration
    val port: Int = config.getInt("http.port")

    val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, host, port)

    val log: LoggingAdapter =  Logging(RestApi.system.eventStream, "RestApiApp")

    bindingFuture.map { serverBinding =>
      log.info(s"RestApi bound to ${serverBinding.localAddress} ") }
    log.info(s"Press RETURN to stop...")

    scala.io.StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => RestApi.system.terminate())
  }
}