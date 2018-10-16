package com.bova.app.actors

import akka.actor.{Actor, ActorRef}

class TransactionActor(receiver :ActorRef) extends Actor {
  var depositedMoney : Double = 0

  override def receive: Receive = {
    case msg : Transaction =>
      sender() ! MoneyRequest(msg.money)
      println(s"=== TransactionActor is processing Transaction with ${msg.money}")
    case msg : MoneyResponse =>
      depositedMoney += msg.money
      println(s"=== TransactionActor is processing MoneyResponse with ${msg.money}")
  }
}