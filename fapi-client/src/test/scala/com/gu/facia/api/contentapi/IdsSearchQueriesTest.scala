package com.gu.facia.api.contentapi

import org.scalatest.{ShouldMatchers, FreeSpec}

class IdsSearchQueriesTest extends FreeSpec with ShouldMatchers {

  "limit batches" - {
    val fifty = List.fill(50)("abc").toSeq
    val fiftyOne = fifty :+ "fiftyFirst"

    "leave batches under or equal to 50" in {
      IdsSearchQueries.makeBatches(fifty) {
        _.mkString("")
      } should be(Some(Seq(fifty)))
    }

    "should limit batches over 50" in {
      IdsSearchQueries.makeBatches(fiftyOne){_.mkString("")} should be (Some(
        Seq(fifty, Seq("fiftyFirst"))))
    }
  }
}
