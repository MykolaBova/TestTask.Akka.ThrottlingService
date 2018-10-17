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


  val router: ActorRef = system.actorOf(Props[RpsCalculatorRoutersPool])

  var calc1 : ActorRef = system.actorOf(Props[RpsCalculatorActor], "rps1")
  var calc2 : ActorRef = system.actorOf(Props[RpsCalculatorActor], "rps2")
  var calc3 : ActorRef = system.actorOf(Props[RpsCalculatorActor], "rps3")
  var calc4 : ActorRef = system.actorOf(Props[RpsCalculatorActor], "rps4")
  var calc5 : ActorRef = system.actorOf(Props[RpsCalculatorActor], "rps5")

  var user: Option[User] = None

  override def receive: Receive = {
    case command : GetAllowedByTokenRequest =>

      implicit val timeout: Timeout = Timeout(10 seconds)
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

      var allowed : Boolean = false

      // Calculate if given operation is allowed or no
      if (user.isDefined) {
        implicit val timeout: Timeout = Timeout(10 seconds)

        log.info(s">> getUserResponse = ${user.get}")
        // ask corresponding Worker
        val getRpsRespFuture: Future[GetRpsResp] =
          (context.actorSelection("/user/"+user.get.user.get) ? GetRps()).mapTo[GetRpsResp]

        getRpsRespFuture.onComplete {
          case Success(getRpsResp) =>
            allowed = getRpsResp.allowed
          case Failure(exception) =>
            log.error(s"** Exception was thrown, $exception")
            allowed = false
        }

        log.info(s">> allowed = $allowed")
      } else {
        log.info(s">> getUserResponse = None")
        // create corresponding worker
        router ! GetCalculateRps(command.token)

        // before SLA will be returned by SlaService, use graceRps for calculating PRS
      }

      sender() ! GetAllowedByTokenResponse(Option(allowed))
      log.info("<== GetAllowedByTokenRequest command")
    case _ =>
  }
}