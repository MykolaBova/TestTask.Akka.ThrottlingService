package com.bova.app.actors.old

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}

object AkkaApp extends App {

  override def main(args : Array[String]) : Unit = {
    createSystem()
  }

  def createSystem() : Unit = {
    val system = ActorSystem("AkaSystem")
    val supervisor = system.actorOf(Props[AccountSuperviserActor], "supervisor")

    while(true) {
      print("Command > ")
      val command : String = scala.io.StdIn.readLine()

      command match {
        case "exit" => System.exit(0)
        case "create account" =>
          print("Enter name > ")
          val name : String = scala.io.StdIn.readLine()
          print("Enter money > ")
          val money : Double = scala.io.StdIn.readDouble()
          val command = AccountCreateCommand(name, money)
          supervisor ! command
        case "show account" =>
          println("Enter id number")
          val idNumber = scala.io.StdIn.readInt()
          supervisor ! ActorRouteRequest(BankId(idNumber))
        case "transaction" =>
          print("Enter sender id > ")
          val senderId = scala.io.StdIn.readInt()
          print("Enter received id > ")
          val sreceiverId = scala.io.StdIn.readInt()
          print("Enter money > ")
          val money : Double = scala.io.StdIn.readDouble()

          implicit val timeout: Timeout = Timeout(10 seconds)
          implicit val ec: ExecutionContextExecutor = ExecutionContext.global
          val transaction = Transaction(UUID.randomUUID(), BankId(senderId), BankId(sreceiverId), money)
          val routeRequest = (supervisor ? ActorRouteRequest(BankId(senderId))).mapTo[ActorRef]

          routeRequest.onComplete{
            case Success(actorRef) =>
              println(s"Transaction has being set to authorized $actorRef")
              actorRef ! transaction
            case Failure(exception) => print(s"== Transaction $transaction failed because of exception $exception")
          }
        case _ =>
          println(s"Unable to process that command $command")
      }
    }
  }

  def cube(x: Int) = x * x * x
}