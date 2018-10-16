package com.bova.app.rest

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.Directives.{get, pathSingleSlash}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.bova.app.services.{SlaService, SlaServiceStub, ThrottlingService, ThrottlingServiceImpl}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.immutable
import scala.concurrent.ExecutionContextExecutor

object  RestApi {
  final val AKKA_SYSTEM_NAME: String = "system"
  final val HTTP_HEADER_NAME_AUTHORIZATION: String = "Authorization"
  final val MSG_USER_IS_NOT_AUTHORIZED: String = " User is not authorized"
  final val MSG_USER_AUTHORIZED: String = " User authorized"
  final val MSG_FUNCTION_CALL_ALLOWED: String = " Function call allowed"
  final val MSG_FUNCTION_CALL_NOT_ALLOWED: String = " Function call is not allowed"

  implicit val system: ActorSystem = ActorSystem(AKKA_SYSTEM_NAME)
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = RestApi.system.dispatcher

  val slaService: SlaService = new SlaServiceStub()
  val throttlingService : ThrottlingService = new ThrottlingServiceImpl()

  val config: Config = ConfigFactory.load()

  def generateHttpResponse(token: Option[String], t: => Unit): String = {
    val log: LoggingAdapter =  Logging(system.eventStream, "RestApi")

    log.info("-> generateHttpResponse")

    if (token.isEmpty) {
      log.info(">" + MSG_USER_IS_NOT_AUTHORIZED)
      log.info("<- generateHttpResponse")
    } else {
      log.info(">" + MSG_USER_AUTHORIZED)
      log.info(">" + s" token == $token")
      log.info("<- generateHttpResponse")
    }

    var resp : String = MSG_FUNCTION_CALL_NOT_ALLOWED
    if(throttlingService.isRequestAllowed(token)) {
      resp  = MSG_FUNCTION_CALL_ALLOWED
      t
      log.info(">" + resp)
    } else {
      log.info(">" + resp)
    }

    resp
  }

  def getHeaderByName(headers: immutable.Seq[HttpHeader], headerName: String) : Option[String] =
  {
    if(headers.isEmpty)
      None
    else {
      val res: Array[(String, String)] =
        headers
          .toArray
          .map(it => it.toString()
            .split(":", 2)
          )
          .filter(_.length == 2)
          .map(x => Tuple2(x(0),x(1)))
          .filter(x => x._1 == headerName)

      if(res.isEmpty) None
      else Option(res(0)._2)
    }
  }
}

trait RestApi {

  val route: Route = buildRout()

  val log: LoggingAdapter =  Logging(RestApi.system.eventStream, "RestApi")

  def buildRout(): Route = {
    val route: Route = pathSingleSlash {

      get { ctx =>
        val headers: immutable.Seq[HttpHeader] = ctx.request.headers
        val token: Option[String] = RestApi.getHeaderByName(headers,
          RestApi.HTTP_HEADER_NAME_AUTHORIZATION)

        ctx.complete {
          RestApi.generateHttpResponse(token, someFunction())
        }
      }
    }
    route
  }

  def someFunction() : Unit = {
    log.info("-> someFunction")
  }
}