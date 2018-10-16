package com.bova.app

import akka.actor.ActorSystem
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

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    //val bindingFuture = Http().bindAndHandle(route, host, port)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    scala.io.StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

  }
}