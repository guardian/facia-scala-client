package com.gu.facia.api.integration

import com.gu.facia.api.FAPI
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
import com.gu.facia.api.models.{Collection, CollectionId}
import com.gu.facia.api.utils.SectionKicker
import lib.IntegrationTestConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{OptionValues, FreeSpec, ShouldMatchers}

class IntegrationTest extends FreeSpec with ShouldMatchers with ScalaFutures with OptionValues with IntegrationTestConfig {
  implicit val patience = PatienceConfig(Span(2, Seconds), Span(50, Millis))

  "getFronts" - {
    "should return a set of Front instances from the fronts JSON" in {
      FAPI.getFronts().asFuture.futureValue.fold(
        err => fail(s"expected fronts, got error $err", err.cause),
        fronts => fronts.size should be > 0
      )
    }
  }

  "frontsForPath" - {
    "should return the front for the given path" in {
      FAPI.frontForPath("uk").asFuture.futureValue.fold(
        err => fail(s"expected front, got error $err", err.cause),
        front => front.id should equal("uk")
      )
    }
  }

  "getCollection" - {
    "should return the collection with a given id" in {
      FAPI.getCollection(CollectionId("uk-alpha/news/regular-stories")).asFuture.futureValue.fold(
        err => fail(s"expected collection, got $err", err.cause),
        collection => collection should have ('id ("uk-alpha/news/regular-stories"))
      )
    }
  }

  "collectionContent" - {
    // fetch collection to use in these tests
    val collection = FAPI.getCollection(CollectionId("uk-alpha/news/regular-stories")).asFuture.futureValue.fold(
      err => fail(s"expected collection, got $err", err.cause),
      collection => collection
    )

    "should return the curated content for the collection" in {
      FAPI.collectionContent(collection).asFuture.futureValue.fold(
        err => fail(s"expected collection, got $err", err.cause),
        curatedContent => curatedContent.size should be > 0
      )
    }

    "will use the provided function to adjust the query used to hydrate content" in {
      val adjust: AdjustSearchQuery = q => q.showTags("tone")
      FAPI.collectionContent(collection, adjust).asFuture.futureValue.fold(
        err => fail(s"expected collection, got $err", err.cause),
        curatedContent => curatedContent.head.content.tags.exists(_.`type` == "tone") should equal(true)
      )
    }
  }

  "backfill" - {
    val collection = Collection(CollectionId("uk/business/regular-stories"), "economy", Nil, None, "updatedBy", "updatedBy@example.com", None,
      Some("business?edition=uk"), "news/most-popular", None, false, false, false, false, false, false)

    "can get the backfill for a collection" in {
      val query = "business?edition=uk"
      FAPI.backfill(query, collection).asFuture.futureValue.fold(
        err => fail(s"expected backfill results, got $err", err.cause),
        backfillContents => backfillContents.size should be > 0
      )
    }

    "collection metadata is resolved on backfill content" in {
      val query = "business?edition=uk"
      FAPI.backfill(query, collection.copy(showSections = true)).asFuture.futureValue.fold(
        err => fail(s"expected backfill results, got $err", err.cause),
        backfillContents => backfillContents.head.kicker.value shouldBe a [SectionKicker]
      )
    }

    "item query can be adjusted" in {
      val query = "business?edition=uk"
      val adjust: AdjustItemQuery = q => q.showTags("tone")
      FAPI.backfill(query, collection.copy(showSections = true), adjustItemQuery = adjust).asFuture.futureValue.fold(
        err => fail(s"expected backfill results, got $err", err.cause),
        backfillContents => backfillContents.head.content.tags.exists(_.`type` == "tone") should equal(true)
      )
    }

    "search query can be adjusted" in {
      val query = "search?tag=sustainable-business/series/finance&use-date=published"
      val adjust: AdjustSearchQuery = q => q.showTags("tone")
      FAPI.backfill(query, collection.copy(showSections = true), adjustSearchQuery = adjust).asFuture.futureValue.fold(
        err => fail(s"expected backfill results, got $err", err.cause),
        backfillContents => backfillContents.head.content.tags.exists(_.`type` == "tone") should equal(true)
      )
    }
  }

  def fail(message: String, cause: Option[Throwable]): Nothing = {
    cause.map(fail(message, _)).getOrElse(fail(message))
  }
}
