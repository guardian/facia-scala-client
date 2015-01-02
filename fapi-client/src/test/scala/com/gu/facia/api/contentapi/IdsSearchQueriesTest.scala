package com.gu.facia.api.contentapi

import org.scalatest.{ShouldMatchers, FreeSpec}

class IdsSearchQueriesTest extends FreeSpec with ShouldMatchers {
  "makeBatches" - {
    "be None if any ID is longer than the max url size" in {
      IdsSearchQueries.makeBatches(Seq("x")) { _ =>
        List.fill(IdsSearchQueries.MaxUrlSize + 1)("a").mkString("")
      } should equal(None)
    }

    "be Some batches, where the url for each batch is shorter than the max url size" in {
      IdsSearchQueries.makeBatches(Seq(
        "a",
        "b",
        "c",
        "d",
        "e",
        "f",
        "g",
        "h",
        "i",
        "j",
        "k"
      )) { ids =>
        List.fill(ids.length * IdsSearchQueries.MaxUrlSize / 4)("a").mkString("")
      } should equal(Some(Seq(
        Seq(
          "a",
          "b",
          "c",
          "d"
        ),
        Seq(
          "e",
          "f",
          "g",
          "h"
        ),
        Seq(
          "i",
          "j",
          "k"
        )
      )))
    }
  }
}
