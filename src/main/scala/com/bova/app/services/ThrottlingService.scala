package com.bova.app.services

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.bova.app.rest.RestApi
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

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

  override def isRequestAllowed(token: Option[String]): Boolean = {
    log.info("--> isRequestAllowed")
    var rps : Int = graceRps

    if(token.isDefined) {
      val f: Future[Sla] = slaService.getSlaByToken(token.get.trim)

      f.onComplete {
        case Success(value) =>
          rps = value.rps
        case Failure(e) =>
          log.error(e.toString)
      }
    }

    log.info(s">> rps == $rps")
    log.info(s">> graceRps == $graceRps")
    log.info("<-- isRequestAllowed")
    rps > graceRps
  }
}