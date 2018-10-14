package com.bova.app

import akka.http.scaladsl.server.Directives.{complete, get, pathSingleSlash}
import akka.http.scaladsl.server.Route

trait RestApi {
  val route: Route =
    pathSingleSlash {
      get {
        complete {
          "Hello world :)"
        }
      }
    }
}