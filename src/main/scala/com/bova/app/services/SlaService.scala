package com.bova.app.services

import com.bova.app.rest.RestApi

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
      case "t1" => Future {Sla("u1", 2)}
      case "t2" => Future {Sla("u1", 2)}
      case "t3" => Future {Sla("u2", 3)}
      case "t4" => Future {Sla("u2", 3)}
      case "t5" => Future {Sla("u3", 4)}
      case "t6" => Future {Sla(""  , 0)}
      case _ => Future.failed(new NoSuchElementException(token))
    }
  }
}