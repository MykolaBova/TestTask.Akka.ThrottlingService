package com.bova.app

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.Directives.{get, pathSingleSlash}
import akka.http.scaladsl.server.Route

import scala.collection.immutable

trait RestApi {
  val route: Route =
    pathSingleSlash {
      get { ctx =>
        val headers: immutable.Seq[HttpHeader] = ctx.request.headers
        val token: String = getHeaderByName(headers, "Authorization")

        ctx.complete {
          s"Hello world :), token == $token"
        }
      }
    }

  // TODO: quick and dirty hack To replace with standard way of retrieving HTTP headers later
  def getHeaderByName(headers: immutable.Seq[HttpHeader], headerName: String) : String = {
    headers
      .toArray
      .map(it => it.toString()
        .split(":", 2))
      .filter(_.length == 2 )
      .map(x => Tuple2(x(0), x(1)))
      .filter(x => x._1 == headerName)(0)
      ._2
  }
}