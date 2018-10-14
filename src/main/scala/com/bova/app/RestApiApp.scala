package com.bova.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object RestApiApp extends RestApi {

  def main(args: Array[String]) {

    implicit val actorSystem: ActorSystem = ActorSystem("system")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    Http().bindAndHandle(route,"localhost",8080)

    println("server started at 8080")
  }
}