package com.bova.app.testable.rest

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.bova.app.rest.RestApi
import org.scalatest.{BeforeAndAfterAll, FlatSpec, MustMatchers}

class ApiSpec extends FlatSpec
  with MustMatchers
  with ScalatestRouteTest
  with BeforeAndAfterAll
  with RestApi {

  // TODO: Very simple test To extend later
  "The Server" should "return Ok response when get all tweets" in {

    Get("/") ~> route ~> check {
      status must equal(StatusCodes.OK)
    }
  }

}