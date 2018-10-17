package com.bova.app.services.throtting.actors

import akka.actor.Actor

case class GetCalculateRps(token : String)

class RpsCalculatorActor extends Actor {
  override def receive: Receive = {
    case command : GetCalculateRps =>
    case _  =>
  }
}
