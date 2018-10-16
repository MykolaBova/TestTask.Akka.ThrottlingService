package com.bova.app.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class AccountActor(val id : BankId, val name : String, var money : Double) extends  Actor {
  override def preStart(): Unit = {
    println(s"== $name has been created with an ID of ${id.idNumber} and a money of $money")
  }

  override def receive: Receive = {
    case  msg : Transaction =>
      implicit val timeout: Timeout = Timeout(10 seconds)
      implicit val ec: ExecutionContextExecutor = ExecutionContext.global
      val routeRequest = (context.parent ? ActorRouteRequest(msg.receiver)).mapTo[ActorRef]
      routeRequest.onComplete{
        case Success(actorRef) =>
          val ref = context.actorOf(Props(new TransactionActor(actorRef)))
          ref ! msg
        case Failure(exception) => print("== Transaction failed")
      }
      println(s"== $name just received ${msg.money}.money and its current amount is $money")
    case msg : MoneyRequest =>
      if(money >= msg.money) {
        money -= msg.money
        sender ! MoneyResponse(msg.money)
        println(s"== Transaction of ${msg.money} ok")
      } else {
        println(s"== Transaction of ${msg.money} failed")
      }
    case msg: MoneyDeposit =>
      money += msg.money
      print(s"== $name received ${msg.money} and current money is $money")
    case _ =>
      println("== Unable to process that message")
  }
}