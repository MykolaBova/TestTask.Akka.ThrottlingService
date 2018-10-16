package com.bova.app.tests.ut

import com.bova.app.actors.old.AkkaApp
import org.scalatest.FunSuite

class AkkaAppTest extends FunSuite {
  test("CubeCalculator.cube") {
    assert(AkkaApp.cube(3) === 27)
  }
  test("CubeCalculator.cube 0") {
    assert(AkkaApp.cube(0) === 0)
  }
}