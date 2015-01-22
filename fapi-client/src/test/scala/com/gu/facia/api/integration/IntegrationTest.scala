package com.gu.facia.api.integration

import com.gu.facia.api.FAPI
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
import com.gu.facia.api.models._
import com.gu.facia.api.utils.SectionKicker
import com.gu.facia.client.models.{CollectionConfigJson, CollectionJson, TrailMetaData, Trail}
import lib.IntegrationTestConfig
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FreeSpec, OptionValues, ShouldMatchers}
import play.api.libs.json.JsString

class IntegrationTest extends FreeSpec with ShouldMatchers with ScalaFutures with OptionValues with IntegrationTestConfig {
  implicit val patience = PatienceConfig(Span(5, Seconds), Span(50, Millis))

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
      FAPI.getCollection("uk-alpha/news/regular-stories").asFuture.futureValue.fold(
        err => fail(s"expected collection, got $err", err.cause),
        collection => collection should have ('id ("uk-alpha/news/regular-stories"))
      )
    }
  }

  "frontCollections" - {
    "should return collections for the given front" in {
      FAPI.frontCollections("uk").asFuture.futureValue.fold(
        err => fail(s"expected collection, got $err", err.cause),
        collections => collections.size should be > 0
      )
    }
  }

  "collectionContent" - {
    // fetch collection to use in these tests
    val collection = FAPI.getCollection("uk-alpha/news/regular-stories").asFuture.futureValue.fold(
      err => fail(s"expected collection, got $err", err.cause),
      collection => collection
    )

    "should return the curated content for the collection" in {
      FAPI.collectionContentWithSnaps(collection).asFuture.futureValue.fold(
        err => fail(s"expected collection, got $err", err.cause),
        curatedContent => curatedContent.size should be > 0
      )
    }

    "will use the provided function to adjust the query used to hydrate content" in {
      val adjust: AdjustSearchQuery = q => q.showTags("tone")
      FAPI.collectionContentWithSnaps(collection, adjust).asFuture.futureValue.fold(
        err => fail(s"expected collection, got $err", err.cause),
        curatedContent => curatedContent.flatMap{
          case c: CuratedContent => Some(c)
          case _ => None
        }.head.content.tags.exists(_.`type` == "tone") should equal(true)
      )
    }

    "for snaps" - {
      def makeLatestTrailFor(id: String, uri: String) =
        Trail(id, 0, Some(TrailMetaData(Map("snapUri" -> JsString(uri), "snapType" -> JsString("latest")))))
      def makeLinkSnapFor(id: String, uri: String) =
        Trail(id, 0, Some(TrailMetaData(Map("snapUri" -> JsString(uri), "snapType" -> JsString("link")))))
      val dreamSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val dreamSnapTwo = makeLatestTrailFor("snap/2372382", "technology")

      val plainSnapOne = makeLinkSnapFor("snap/347234723", "doesnotmatter")

      val normalTrail = Trail("internal-code/content/445034105", 0, None)

      def makeCollectionJson(trails: Trail*) = CollectionJson(
        live = trails.toList,
        draft = None,
        lastUpdated = new DateTime(1),
        updatedBy = "test",
        updatedEmail = "test@example.com",
        displayName = Some("displayName"),
        href = Some("href"))
      val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

      "should turn dream snaps into content" in {
        val collectionJson = makeCollectionJson(dreamSnapOne, dreamSnapTwo)
        val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
        val faciaContent = FAPI.collectionContentWithSnaps(collection)

        faciaContent.asFuture.futureValue.fold(
          err => fail(s"expected to get two dream snaps, got $err", err.cause),
          {listOfFaciaContent =>
            val dreamSnaps = listOfFaciaContent.collect{ case ls: LatestSnap => ls}
            dreamSnaps.length should be (2)
            dreamSnaps(0).latestContent.fold(fail("dream snap 0 content was empty")){ c =>
              c.tags.exists(_.id.contains("culture"))
            }
            dreamSnaps(1).latestContent.fold(fail("dream snap 1 content was empty")){ c =>
              c.tags.exists(_.id.contains("technology"))
            }
          }
        )
      }

      "work with normal content and link snaps" in {
        val collectionJson = makeCollectionJson(dreamSnapOne, normalTrail, plainSnapOne, dreamSnapTwo)
        val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
        val faciaContent = FAPI.collectionContentWithSnaps(collection)

        faciaContent.asFuture.futureValue.fold(
          err => fail(s"expected to get three items, got $err", err.cause),
          { listOfFaciaContent =>
            listOfFaciaContent.length should be (4)

            val normalContent = listOfFaciaContent.collect{ case cc: CuratedContent => cc}
            normalContent.length should be (1)
            normalContent.apply(0).headline should be ("PM returns from holiday after video shows US reporter beheaded by Briton")

            val latestSnapContent = listOfFaciaContent.collect{ case ls: LatestSnap => ls}
            latestSnapContent.length should be (2)
            latestSnapContent.forall(_.latestContent == None) should be (false)

            val linkSnaps = listOfFaciaContent.collect{ case ls: LinkSnap => ls}
            linkSnaps.length should be (1)
            linkSnaps(0).id should be ("snap/347234723")
            linkSnaps(0).snapUri should be (Some("doesnotmatter"))

          }
        )
      }
      "not request dream snaps in" in {
        val collectionJson = makeCollectionJson(dreamSnapOne, normalTrail, dreamSnapTwo)
        val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
        val faciaContent = FAPI.collectionContentWithoutSnaps(collection)

        faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected to get three items, got $err", err.cause),
        { listOfFaciaContent =>
          listOfFaciaContent.length should be (3)

          val normalContent = listOfFaciaContent.collect{ case cc: CuratedContent => cc}
          normalContent.apply(0).headline should be ("PM returns from holiday after video shows US reporter beheaded by Briton")

          val latestSnapContent = listOfFaciaContent.collect{ case ls: LatestSnap => ls}
          latestSnapContent.length should be (2)
          latestSnapContent.forall(_.latestContent == None) should be (true)
        })
      }


    }
  }

  "backfill" - {
    val collection = Collection(
      "uk/business/regular-stories",
      "economy",
      Nil,
      None,
      Some(new DateTime(1)),
      Some("updatedBy"),
      Some("updatedBy@example.com"),
      None,
      Some("business?edition=uk"),
      "news/most-popular",
      None,
      false,
      false,
      false,
      false,
      false,
      false
    )

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
      val adjust: AdjustItemQuery = q => q.showTags("all")
      FAPI.backfill(query, collection.copy(showSections = true), adjustItemQuery = adjust).asFuture.futureValue.fold(
        err => fail(s"expected backfill results, got $err", err.cause),
        backfillContents => backfillContents.head.content.tags.exists(_.id.contains("business")) should equal(true)
      )
    }

    "search query can be adjusted" in {
      val query = "search?tag=sustainable-business/series/finance&use-date=published"
      val adjust: AdjustSearchQuery = q => q.showTags("series")
      FAPI.backfill(query, collection.copy(showSections = true), adjustSearchQuery = adjust).asFuture.futureValue.fold(
        err => fail(s"expected backfill results, got $err", err.cause),
        backfillContents => backfillContents.head.content.tags.exists(_.id.contains("sustainable-business/series/finance")) should equal(true)
      )
    }
  }

  def fail(message: String, cause: Option[Throwable]): Nothing = {
    cause.map(fail(message, _)).getOrElse(fail(message))
  }
}
