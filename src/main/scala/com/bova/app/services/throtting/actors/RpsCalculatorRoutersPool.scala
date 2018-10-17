package com.bova.app.services.throtting.actors

import akka.actor.{Actor, ActorRef, Props}

class RpsCalculatorRoutersPool extends Actor {

  var routees: List[ActorRef] = _

  override def preStart(): Unit = {
    routees = List.fill(5)(
      context.actorOf(Props[RpsCalculatorActor])
    )
  }

  def receive(): PartialFunction[Any, Unit] = {
    case msg: GetCalculateRps =>
      println("=> I'm A Router and I received a Message.....")
      routees(util.Random.nextInt(routees.size)) forward msg
  }
}