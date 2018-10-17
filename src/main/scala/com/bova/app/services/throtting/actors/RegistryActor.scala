package com.bova.app.services.throtting.actors

import akka.actor.{Actor, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import com.bova.app.rest.RestApi

import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor

object UserStatus {
  sealed trait UserStatusVal
  case object Active extends UserStatusVal
  case object Creating extends UserStatusVal
  case object Deleting extends UserStatusVal
}

case class User(user : Option[String], status : UserStatus.UserStatusVal)

case class GetUserRequest(token : String)
case class GetUserResponse(user : Option[User])
case class SetUserRequest(token : String, user : User)

class RegistryActor extends Actor {

  var users: mutable.Map[String, User] = scala.collection.mutable.Map[String, User]()

  implicit val system: ActorSystem = RestApi.system
  implicit val actorMaterializer: ActorMaterializer = RestApi.actorMaterializer
  implicit val ec: ExecutionContextExecutor = RestApi.ec

  val log: LoggingAdapter =  Logging(RestApi.system.eventStream, "RegistryActor")

  var user: Option[User] = None

  override def receive: Receive = {
    case command : GetUserRequest =>
      log.info("<=== GetUserRequest command")

      if(users.contains(command.token)) {
        user = Option(users(command.token))
      } else {
        user = None
      }
      log.info(s"<<< user = $user")
      sender() ! GetUserResponse(user)
      log.info("===> GetUserRequest command")
    case command : SetUserRequest =>
      log.info(s"<=== SetUserRequest command $command")
      users += (command.token -> command.user)
      log.info("===> SetUserRequest command")
    case _ =>
  }
}
