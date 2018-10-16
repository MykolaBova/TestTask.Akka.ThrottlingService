package com.bova.app.actors.old

import akka.actor.{Actor, Props}

case class AccountCreateCommand(name : String, money : Double)
case class ActorRouteRequest(id : BankId)
class AccountSuperviserActor extends Actor {
  var count = 1

  override def receive: Receive = {
    case command : AccountCreateCommand =>
      val ref = context.actorOf(Props(new AccountActor(BankId(count), command.name, command.money)),count.toString)
      count += 1
      println(s"= created a new account $ref")
      println(s"= Path for created Actor is ${ref.path}")
      println(s"= Path name for created Actor is ${ref.path.name}")
    case command : ActorRouteRequest =>
      context.child(command.id.idNumber.toString) match {
        case Some(actorRef) => sender() ! actorRef
        case None => sender() ! new RuntimeException("Actor not found")
      }
    case _ =>
      println("= Unable to process that message")
  }
}