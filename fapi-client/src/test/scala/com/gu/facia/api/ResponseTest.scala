package com.gu.facia.api

import org.scalatest.concurrent.ScalaFutures
import org.scalatest._
import scala.concurrent.ExecutionContext.Implicits.global

class ResponseTest extends FreeSpec with ShouldMatchers with ScalaFutures {

  "Response.filter" - {
    "should return the same result if the filter function returns true" in {
      Response.Right[Int](4).filter(_ == 4).asFuture.futureValue should equal(Right(4))
    }

    "should return a Left(FilteredOut) if the filter function returns false" in {
      Response.Right[Int](4).filter(_ == 3).asFuture.futureValue should equal(Left(FilteredOut))
    }
  }
}
