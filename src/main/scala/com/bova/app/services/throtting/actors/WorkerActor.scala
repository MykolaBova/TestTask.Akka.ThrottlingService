package com.bova.app.services.throtting.actors

import akka.actor.{Actor, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import com.bova.app.rest.RestApi

import scala.concurrent.ExecutionContextExecutor

case class GetRps()
case class GetRpsResp(allowed: Boolean)

object WorkerActor {

  def calculateAllowedCalls(allowedCalls : Long, updated : Long, sla: Int) : Long = {
    var localAllowedCalls: Long = allowedCalls

    val timePassed: Long = System.currentTimeMillis - updated

    // 1 s == 10000 ms
    var ratio = timePassed / 100

    localAllowedCalls += ratio
    if (localAllowedCalls > sla) localAllowedCalls = sla

    localAllowedCalls
  }
}

class WorkerActor(sla : Int) extends Actor {

  implicit val system: ActorSystem = RestApi.system
  implicit val actorMaterializer: ActorMaterializer = RestApi.actorMaterializer
  implicit val ec: ExecutionContextExecutor = RestApi.ec

  val log: LoggingAdapter =  Logging(RestApi.system.eventStream, "DispatcherActor")

  val created : Long = System.currentTimeMillis
  var updated : Long = System.currentTimeMillis

  var allowedCalls : Long = sla

  override def receive: Receive = {
    case command : GetRps =>
      allowedCalls -= 1

      allowedCalls = WorkerActor.calculateAllowedCalls(allowedCalls, updated, sla)

      updated = System.currentTimeMillis

      val allowed = if(allowedCalls > 0) true
        else false

      log.info(s">>>>> allowedCalls = $allowedCalls")
      log.info(s">>>>> allowed = $allowed")

      sender() ! GetRpsResp(allowed)
    case _ =>
  }
}