package com.bova.app.services.throtting.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import com.bova.app.rest.RestApi

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

case class GetAllowedByTokenRequest(token : String)
case class GetAllowedByTokenResponse(allowed : Option[Boolean])

class DispatcherActor extends Actor {

  implicit val system: ActorSystem = RestApi.system
  implicit val actorMaterializer: ActorMaterializer = RestApi.actorMaterializer
  implicit val ec: ExecutionContextExecutor = RestApi.ec

  val log: LoggingAdapter =  Logging(RestApi.system.eventStream, "DispatcherActor")

  implicit val timeout: Timeout = Timeout(10 seconds)

  val registry: ActorRef = system.actorOf(Props[RegistryActor], "registry")

  var user: Option[User] = None

  override def receive: Receive = {
    case command : GetAllowedByTokenRequest =>
      log.info("==> GetAllowedByTokenRequest command")

      // Get User from Registry by token
      val getUserResponseFuture: Future[GetUserResponse] =
        (registry ? GetUserRequest(command.token)).mapTo[GetUserResponse]

      getUserResponseFuture.onComplete {
        case Success(getUserResponse) =>
          user = getUserResponse.user
        case Failure(exception) =>
          log.error(s"** Exception was thrown, $exception")
          user = None
      }

      // Calculate if given operation is allowed or no
      if (user.isDefined) {
        log.info(s">> getUserResponse = ${user.get}")
        // ask corresponding Worker
      } else {
        log.info(s">> getUserResponse = None")
        // create corresponding worker
        // before SLA will be returned by SlaService, use graceRps for calculating PRS
      }

      sender() ! GetAllowedByTokenResponse(None)
      log.info("<== GetAllowedByTokenRequest command")
    case _ =>
  }
}