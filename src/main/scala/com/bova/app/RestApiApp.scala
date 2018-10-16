package com.bova.app

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.bova.app.rest.RestApi
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor

object RestApiApp extends RestApi {

  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem("system")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val config = ConfigFactory.load()
    val host = config.getString("http.host") // Gets the host and a port from the configuration
    val port = config.getInt("http.port")

    val bindingFuture = Http().bindAndHandle(route, host, port)

    val log =  Logging(system.eventStream, "bova-sys")
    bindingFuture.map { serverBinding =>
      log.info(s"RestApi bound to ${serverBinding.localAddress} ") }

    log.info(s"Server online at http://localhost:8080/")
    log.info(s"Press RETURN to stop...")
    scala.io.StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

  }
}