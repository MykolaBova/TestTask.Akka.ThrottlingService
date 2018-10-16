package com.bova.app.services

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.bova.app.rest.RestApi
import com.bova.app.services.throtting.actors.{DispatcherActor, GetAllowedByTokenRequest, GetAllowedByTokenResponse}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

trait ThrottlingService {
  val graceRps:Int // configurable
  val slaService: SlaService // use mocks/stubs for testing

  // Should return true if the request is within allowed RPS.
  def isRequestAllowed(token:Option[String]): Boolean
}

class ThrottlingServiceImpl extends ThrottlingService {
  val config: Config = ConfigFactory.load()

  override val graceRps: Int = config.getInt("http.graceRps")
  override val slaService: SlaService = RestApi.slaService

  implicit val system: ActorSystem = RestApi.system
  implicit val actorMaterializer: ActorMaterializer = RestApi.actorMaterializer
  implicit val ec: ExecutionContextExecutor = RestApi.ec

  implicit val timeout: Timeout = Timeout(10 seconds)

  val log: LoggingAdapter =  Logging(RestApi.system.eventStream, "ThrottlingServiceImpl")

  val dispatcher: ActorRef = system.actorOf(Props[DispatcherActor], "dispatcher")

  override def isRequestAllowed(token: Option[String]): Boolean = {
    log.info("--> isRequestAllowed")
    var allowed : Boolean = false

    if(token.isDefined) {
      val getAllowedByTokenResponseFuture: Future[GetAllowedByTokenResponse] =
        (dispatcher ? GetAllowedByTokenRequest(token.get.trim)).mapTo[GetAllowedByTokenResponse]

      getAllowedByTokenResponseFuture.onComplete {
        case Success(getSlaByTokenResponse) =>
          if(getSlaByTokenResponse.allowed.isDefined) {
            log.info(s">> getSlaByTokenResponse = ${getSlaByTokenResponse.allowed.get}")
            allowed = getSlaByTokenResponse.allowed.get
          } else {
            log.info(s">> getSlaByTokenResponse = None")
          }
        case Failure(exception) =>
          log.error(s"** Exception was thrown, $exception")
      }
    }

    log.info(s">> allowed == $allowed")
    log.info("<-- isRequestAllowed")
    allowed
  }
}