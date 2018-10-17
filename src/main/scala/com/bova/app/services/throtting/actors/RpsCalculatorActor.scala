package com.bova.app.services.throtting.actors

import akka.actor.{Actor, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import com.bova.app.rest.RestApi

import scala.concurrent.ExecutionContextExecutor

case class GetCalculateRps(token : String)

class RpsCalculatorActor extends Actor {

  implicit val system: ActorSystem = RestApi.system
  implicit val actorMaterializer: ActorMaterializer = RestApi.actorMaterializer
  implicit val ec: ExecutionContextExecutor = RestApi.ec

  val log: LoggingAdapter =  Logging(RestApi.system.eventStream, "RpsCalculatorActor")

  override def receive: Receive = {
    case command : GetCalculateRps =>
      log.info(s">>>> I received Work Message and My ActorRef: ${self}")
      log.info(s">>>> I received Work Message and My ActorRefToken: ${command.token}")
    case _  =>
  }
}
