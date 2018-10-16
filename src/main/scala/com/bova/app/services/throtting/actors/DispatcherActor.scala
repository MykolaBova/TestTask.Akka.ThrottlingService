package com.bova.app.services.throtting.actors

import akka.actor.Actor

case class AllowedByTokenRequest(token : String)
case class AllowedByTokenResponse(allowed : Option[Boolean])

class DispatcherActor extends Actor {
  override def receive: Receive = {
    case command : AllowedByTokenRequest =>
      sender() ! AllowedByTokenResponse(None)
    case _ =>
  }
}