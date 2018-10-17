package com.bova.app.services.throtting.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.bova.app.rest.RestApi
import com.bova.app.services.Sla

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

case class GetCalculateRps(token : String)

class RpsCalculatorActor extends Actor {

  implicit val system: ActorSystem = RestApi.system
  implicit val actorMaterializer: ActorMaterializer = RestApi.actorMaterializer
  implicit val ec: ExecutionContextExecutor = RestApi.ec

  val log: LoggingAdapter =  Logging(RestApi.system.eventStream, "RpsCalculatorActor")

  implicit val timeout: Timeout = Timeout(10 seconds)

  override def receive: Receive = {
    case command : GetCalculateRps =>
      log.info(s">>>> I received Work Message and My ActorRef: ${self}")
      log.info(s">>>> I received Work Message and My ActorRefToken: ${command.token}")

      context.actorSelection("/user/registry") ! SetUserRequest(command.token, User(None, UserStatus.Creating))

      var f: Future[Sla] = RestApi.slaService.getSlaByToken(command.token)

      f.onComplete {
        case Success(value) =>
          log.info(s">>>> Got the callback, meaning = $value")

          val worker: ActorRef =
            system.actorOf(Props(new WorkerActor(value.rps)), value.user)
          log.info(s"Created worker with path ${worker.path}")

          context.actorSelection("/user/registry") ! SetUserRequest(command.token, User(Option(value.user), UserStatus.Active))

        case Failure(e) => log.error(e.toString)
      }

    case _  =>
  }
}
