package com.bova.app.rest

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.Directives.{get, pathSingleSlash}
import akka.http.scaladsl.server.Route

import scala.collection.immutable

object  RestApi {
  final val HTTP_HEADER_NAME_AUTHORIZATION = "Authorization"

  def generateHttpResponse(token: Option[String]): String = {
    if (token.isEmpty) {
      s"User is not authorized"
    } else {
      s"Hello world :), token == $token"
    }
  }

  def getHeaderByName(headers: immutable.Seq[HttpHeader], headerName: String) : Option[String] = {
    if(headers.isEmpty)
      None
    else {
      val res =
        headers
          .toArray
          .map(it => it.toString()
            .split(":", 2)
          )
          .filter(_.length == 2)
          .map(x => Tuple2(x(0), x(1)))
          .filter(x => x._1 == headerName)

      if(res.isEmpty) None
      else Option(res(0)._2)
    }
  }
}

trait RestApi {
  val route: Route =
    pathSingleSlash {
      get { ctx =>
        val headers: immutable.Seq[HttpHeader] = ctx.request.headers
        val token: Option[String] = RestApi.getHeaderByName(headers, RestApi.HTTP_HEADER_NAME_AUTHORIZATION)

        ctx.complete {
          RestApi.generateHttpResponse(token)
        }
      }
    }
}