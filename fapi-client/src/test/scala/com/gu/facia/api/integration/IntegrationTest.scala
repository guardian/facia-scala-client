package com.gu.facia.api.integration

import com.gu.facia.api.FAPI
import com.gu.facia.api.contentapi.ContentApi.AdjustSearchQuery
import com.gu.facia.api.models.{MostPopular, Collection}
import lib.IntegrationTestConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{FreeSpec, Ignore, ShouldMatchers}


class IntegrationTest extends FreeSpec with ShouldMatchers with ScalaFutures with IntegrationTestConfig {
  implicit val patience = PatienceConfig(Span(500, Millis))

  "getFronts" - {
    "should return a set of Front instances from the fronts JSON" in {
      FAPI.getFronts().asFuture.futureValue.fold(
        err => fail(s"expected fronts, got error $err"),
        fronts => fronts.size should be > 0
      )
    }
  }

  "frontsForPath" - {
    "should return the front for the given path" in {
      FAPI.frontForPath("uk").asFuture.futureValue.fold(
        err => fail(s"expected front, got error $err"),
        front => front.id should equal("uk")
      )
    }
  }

  "getCollection" - {
    "should return the collection at a given path" in {
      FAPI.getCollection("uk-alpha/news/regular-stories").fold(
        err => fail(s"expected collection, got $err"),
        collection => collection.live.size should be > 0
      )
    }

    "will use the provided function to adjust the query used to hydrate content" in {
      val adjust: AdjustSearchQuery = q => q.showTags("tone")
      FAPI.getCollection("uk-alpha/news/regular-stories").fold(
        err => fail(s"expected collection, got $err"),
        collection => collection.live.head.content.tags.exists(_.`type` == "tone") should equal(true)
      )
    }
  }

  "backfill" - {
    val collection = Collection("economy", Nil, None, "updatedBy", "updatedBy@example.com", None,
      Some("business?edition=uk"), MostPopular, None, false, false, false, false, false, false)

    "can get the backfill for a collection" ignore {}

    "collection metadata is resolved on backfill content" ignore {}

    "item query can be adjusted" ignore {}

    "search query can be adjusted" ignore {}
  }
}
