package com.bova.app.services

import com.bova.app.throtting.rest.RestApi

import scala.concurrent.{ExecutionContextExecutor, Future}

case class Sla(user:String, rps:Int)

trait SlaService {
  def getSlaByToken(token:String) : Future[Sla]
}

class SlaServiceStub extends SlaService {

  implicit val ec: ExecutionContextExecutor = RestApi.ec

  override def getSlaByToken(token: String): Future[Sla] = {

    Thread.sleep(250) // 250 millisecond

    token match {
      case "t1" => Future {Sla(token, 1)}
      case "t2" => Future {Sla(token, 2)}
      case "t3" => Future {Sla(token, 3)}
      case _ => Future.failed(new NoSuchElementException(token))
    }
  }
}