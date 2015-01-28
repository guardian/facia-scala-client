package com.gu.facia.api

import com.gu.facia.api.Response
import lib.ExecutionContext
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest._
import org.mockito.Mockito._
import scala.concurrent.ExecutionContext.Implicits.global

class ResponseTest extends FreeSpec with ShouldMatchers with ScalaFutures {

  "Response.filter" - {
    "should return the same result if the filter function returns true" in {
      Response.Right[Int](4).filter(_ == 4).asFuture.futureValue should equal(Right(4))
    }

    "should return a Left(FilteredOut) if the filter function returns false" in {
      Response.Right[Int](4).filter(_ == 3).asFuture.futureValue should equal(Left(FilteredOut()))
    }
  }
}
