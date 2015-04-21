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
import play.api.libs.json.{Json, JsArray, JsString}

class IntegrationTest extends FreeSpec with ShouldMatchers with ScalaFutures with OptionValues with IntegrationTestConfig {
  implicit val patience = PatienceConfig(Span(5, Seconds), Span(50, Millis))

  def makeCollectionJson(trails: Trail*) = CollectionJson(
    live = trails.toList,
    draft = None,
    treats = None,
    lastUpdated = new DateTime(1),
    updatedBy = "test",
    updatedEmail = "test@example.com",
    displayName = Some("displayName"),
    href = Some("href"),
    previously = None)

  def makeLatestTrailFor(id: String, uri: String) =
    Trail(id, 0, Some(TrailMetaData(Map("snapUri" -> JsString(uri), "snapType" -> JsString("latest")))))
  def makeLinkSnapFor(id: String, uri: String) =
    Trail(id, 0, Some(TrailMetaData(Map("snapUri" -> JsString(uri), "snapType" -> JsString("link")))))

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
      FAPI.liveCollectionContentWithSnaps(collection).asFuture.futureValue.fold(
        err => fail(s"expected collection, got $err", err.cause),
        curatedContent => curatedContent.size should be > 0
      )
    }

    "will use the provided function to adjust the query used to hydrate content" in {
      val adjust: AdjustSearchQuery = q => q.showTags("tone")
      FAPI.liveCollectionContentWithSnaps(collection, adjust).asFuture.futureValue.fold(
        err => fail(s"expected collection, got $err", err.cause),
        curatedContent => curatedContent.flatMap{
          case c: CuratedContent => Some(c)
          case _ => None
        }.head.content.tags.exists(_.`type` == "tone") should equal(true)
      )
    }

    "for snaps" - {
      val dreamSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val dreamSnapTwo = makeLatestTrailFor("snap/2372382", "technology")

      val plainSnapOne = makeLinkSnapFor("snap/347234723", "doesnotmatter")

      val normalTrail = Trail("internal-code/content/445034105", 0, None)

      val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

      "should turn dream snaps into content" in {
        val collectionJson = makeCollectionJson(dreamSnapOne, dreamSnapTwo)
        val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
        val faciaContent = FAPI.liveCollectionContentWithSnaps(collection)

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
        val faciaContent = FAPI.liveCollectionContentWithSnaps(collection)

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
        val faciaContent = FAPI.liveCollectionContentWithoutSnaps(collection)

        faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected to get three items, got $err", err.cause),
        { listOfFaciaContent =>
          listOfFaciaContent.length should be (1)

          val normalContent = listOfFaciaContent.collect{ case cc: CuratedContent => cc}
          normalContent.apply(0).headline should be ("PM returns from holiday after video shows US reporter beheaded by Briton")
        })
      }


    }
  }

  "backfill" - {
    val collection = Collection(
      "uk/business/regular-stories",
      "economy",
      None,
      Nil,
      None,
      Nil,
      Some(new DateTime(1)),
      Some("updatedBy"),
      Some("updatedBy@example.com"),
      CollectionConfig.empty
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
      FAPI.backfill(query, collection.copy(collectionConfig = collection.collectionConfig.copy(showSections = true))).asFuture.futureValue.fold(
        err => fail(s"expected backfill results, got $err", err.cause),
        backfillContents => backfillContents.head.kicker.value shouldBe a [SectionKicker]
      )
    }

    "item query can be adjusted" in {
      val query = "business?edition=uk"
      val adjust: AdjustItemQuery = q => q.showTags("all")
      FAPI.backfill(query, collection.copy(collectionConfig = collection.collectionConfig.copy(showSections = true)), adjustItemQuery = adjust).asFuture.futureValue.fold(
        err => fail(s"expected backfill results, got $err", err.cause),
        backfillContents => backfillContents.head.content.tags.exists(_.id.contains("business")) should equal(true)
      )
    }

    "search query can be adjusted" in {
      val query = "search?tag=sustainable-business/series/finance&use-date=published"
      val adjust: AdjustSearchQuery = q => q.showTags("series")
      FAPI.backfill(query, collection.copy(collectionConfig = collection.collectionConfig.copy(showSections = true)), adjustSearchQuery = adjust).asFuture.futureValue.fold(
        err => fail(s"expected backfill results, got $err", err.cause),
        backfillContents => backfillContents.head.content.tags.exists(_.id.contains("sustainable-business/series/finance")) should equal(true)
      )
    }
  }

  "Supporting Items" - {
    def makeTrail(id: String) =
      Trail(id, 0, None)
    def makeTrailWithSupporting(id: String, supporting: Trail*) =
      Trail(id, 0, Some(TrailMetaData(Map("supporting" -> JsArray(supporting.map(Json.toJson(_)))))))

    val supportingTrailOne = makeTrail("internal-code/content/445034105")
    val supportingTrailTwo = makeTrail("internal-code/content/445529464")

    val trailWithSupporting = makeTrailWithSupporting("internal-code/content/454695023", supportingTrailOne, supportingTrailTwo)

    val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())
    val collectionJson = makeCollectionJson(trailWithSupporting)

    val dreamSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
    val dreamSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
    val plainSnapOne = makeLinkSnapFor("snap/347234723", "doesnotmatter")
    val trailWithSupportingAndDreamSnaps = makeTrailWithSupporting("internal-code/content/454695023", supportingTrailOne, dreamSnapOne, dreamSnapTwo, supportingTrailTwo, plainSnapOne)
    val collectionJsonSupportingWithDreamSnaps = makeCollectionJson(trailWithSupportingAndDreamSnaps)

    "should be filled correctly" in {
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Option(collectionJson), collectionConfig)
      val faciaContent = FAPI.liveCollectionContentWithoutSnaps(collection)

      faciaContent.asFuture.futureValue.fold(
      err => fail(s"expected to get one item with supporting, got $err", err.cause),
      { listOfFaciaContent =>
        listOfFaciaContent.length should be (1)

        listOfFaciaContent.apply(0) match {
          case c: CuratedContent =>
            c.supportingContent.length should be (2)
          case somethingElse => fail(s"expected only CuratedContent, got ${somethingElse.getClass.getName}")
        }
      })
    }

    "should not fill in dream snaps in supporting items" in {
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Option(collectionJsonSupportingWithDreamSnaps), collectionConfig)
      val faciaContent = FAPI.liveCollectionContentWithoutSnaps(collection)

      faciaContent.asFuture.futureValue.fold(
      err => fail(s"expected to get one item with supporting and dream snaps, got $err", err.cause),
      { listOfFaciaContent =>
        listOfFaciaContent.length should be (1)

        listOfFaciaContent.apply(0) match {
          case c: CuratedContent =>
            c.supportingContent.length should be (5)
            c.supportingContent.collect{case s: SupportingCuratedContent => s}.length should be (2)
            val latestSnaps = c.supportingContent.collect{case l: LatestSnap => l}
            latestSnaps.length should be (2)
            latestSnaps.forall(_.latestContent.isDefined == false) should be (true)
            c.supportingContent.collect{case l: LinkSnap => l}.length should be (1)
          case somethingElse => fail(s"expected only CuratedContent, got ${somethingElse.getClass.getName}")
        }
      })
    }

    "should fill in dream snaps in supporting items" in {
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Option(collectionJsonSupportingWithDreamSnaps), collectionConfig)
      val faciaContent = FAPI.liveCollectionContentWithSnaps(collection)

      faciaContent.asFuture.futureValue.fold(
      err => fail(s"expected to get one item with supporting and dream snaps, got $err", err.cause),
      { listOfFaciaContent =>
        listOfFaciaContent.length should be (1)

        listOfFaciaContent.apply(0) match {
          case c: CuratedContent =>
            c.supportingContent.length should be (5)
            val supportingContent = c.supportingContent.collect{case s: SupportingCuratedContent => s}
            supportingContent.length should be (2)
            val latestSnaps = c.supportingContent.collect{case l: LatestSnap => l}
            latestSnaps.length should be (2)
            latestSnaps.forall(_.latestContent.isDefined) should be (true)
            val linkSnaps = c.supportingContent.collect{case l: LinkSnap => l}
            linkSnaps.length should be (1)

            c.supportingContent(0).asInstanceOf[SupportingCuratedContent].headline should be ("PM returns from holiday after video shows US reporter beheaded by Briton")
            c.supportingContent(1).asInstanceOf[LatestSnap].latestContent.get.sectionId should be (Some("culture"))
            c.supportingContent(2).asInstanceOf[LatestSnap].latestContent.get.sectionId should be (Some("technology"))
            c.supportingContent(3).asInstanceOf[SupportingCuratedContent].headline should be ("Inside the 29 August edition")
            c.supportingContent(4).asInstanceOf[LinkSnap].id should be ("snap/347234723")

          case somethingElse => fail(s"expected only CuratedContent, got ${somethingElse.getClass.getName}")
        }
      })
    }
  }

  "Treats" - {
    def makeCollectionJsonWithTreats(treats: Trail*) = CollectionJson(
      live = Nil,
      draft = None,
      treats = Option(treats.toList),
      lastUpdated = new DateTime(1),
      updatedBy = "test",
      updatedEmail = "test@example.com",
      displayName = Some("displayName"),
      href = Some("href"),
      previously = None)

    val normalTrail = Trail("internal-code/content/445034105", 0, None)
    val normalTrailTwo = Trail("internal-code/content/445529464", 0, None)
    val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

    "should request normal item treats" in {
      val collectionJson = makeCollectionJsonWithTreats(normalTrail, normalTrailTwo)
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
      val faciaContent = FAPI.getTreatsForCollection(collection)

      faciaContent.asFuture.futureValue.fold(
          err => fail(s"expected 2 treat result, got $err", err.cause),
          treatsContents => {
            treatsContents.size should be(2)
            treatsContents.head.asInstanceOf[CuratedContent].headline should be ("PM returns from holiday after video shows US reporter beheaded by Briton")
            treatsContents.apply(1).asInstanceOf[CuratedContent].headline should be ("Inside the 29 August edition")
          })
    }

    "should request dream snap treats" in {
      val dreamSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val dreamSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
      val collectionJson = makeCollectionJsonWithTreats(dreamSnapOne, dreamSnapTwo)
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
      val faciaContent = FAPI.getTreatsForCollection(collection, adjustItemQuery = itemQuery => itemQuery.showTags("all"))

      faciaContent.asFuture.futureValue.fold(
          err => fail(s"expected 2 treat result, got $err", err.cause),
          treatsContents => {
            treatsContents.size should be(2)
            treatsContents.head.asInstanceOf[LatestSnap].latestContent.get.tags.exists(_.sectionId == Some("culture")) should be (true)
            treatsContents.apply(1).asInstanceOf[LatestSnap].latestContent.get.tags.exists(_.sectionId == Some("technology")) should be (true)
          })
    }

    "Should request a mix of both" in {
      val dreamSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val dreamSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
      val collectionJson = makeCollectionJsonWithTreats(dreamSnapOne, normalTrail, dreamSnapTwo, normalTrailTwo)
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
      val faciaContent = FAPI.getTreatsForCollection(collection, adjustItemQuery = itemQuery => itemQuery.showTags("all"))

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 2 treat result, got $err", err.cause),
        treatsContents => {
          treatsContents.size should be(4)
          treatsContents.head.asInstanceOf[LatestSnap].latestContent.get.tags.exists(_.sectionId == Some("culture")) should be (true)
          treatsContents.apply(1).asInstanceOf[CuratedContent].headline should be ("PM returns from holiday after video shows US reporter beheaded by Briton")
          treatsContents.apply(2).asInstanceOf[LatestSnap].latestContent.get.tags.exists(_.sectionId == Some("technology")) should be (true)
          treatsContents.apply(3).asInstanceOf[CuratedContent].headline should be ("Inside the 29 August edition")

        })

    }
  }

  "Draft" - {
    def makeCollectionJsonWithTreats(draft: List[Trail], live: Trail*) = CollectionJson(
      live = live.toList,
      draft = Option(draft),
      treats = None,
      lastUpdated = new DateTime(1),
      updatedBy = "test",
      updatedEmail = "test@example.com",
      displayName = Some("displayName"),
      href = Some("href"),
      previously = None)

    val normalTrail = Trail("internal-code/content/445034105", 0, None)
    val normalTrailTwo = Trail("internal-code/content/445529464", 0, None)
    val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

    "should request draft items" in {
      val collectionJson = makeCollectionJsonWithTreats(List(normalTrail, normalTrailTwo))
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
      val faciaContent = FAPI.draftCollectionContentWithoutSnaps(collection)

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 2 treat result, got $err", err.cause),
        contents => {
          contents.size should be(2)
          contents.head.asInstanceOf[CuratedContent].headline should be ("PM returns from holiday after video shows US reporter beheaded by Briton")
          contents.apply(1).asInstanceOf[CuratedContent].headline should be ("Inside the 29 August edition")
        })
    }

    "should return nothing" in {
      val collectionJson = makeCollectionJsonWithTreats(List(normalTrail, normalTrailTwo))
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
      val faciaContent = FAPI.liveCollectionContentWithoutSnaps(collection)

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 2 treat result, got $err", err.cause),
        contents => {
          contents.size should be(0)
        })
    }

    "should request dreamsnaps in draft" in {
      val dreamSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val dreamSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
      val collectionJson = makeCollectionJsonWithTreats(List(dreamSnapOne, dreamSnapTwo))
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
      val faciaContent = FAPI.draftCollectionContentWithSnaps(collection, adjustSnapItemQuery = itemQuery => itemQuery.showTags("all"))

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 2 results, got $err", err.cause),
        contents => {
          contents.size should be(2)
          contents.head.asInstanceOf[LatestSnap].latestContent.get.tags.exists(_.sectionId == Some("culture")) should be (true)
          contents.apply(1).asInstanceOf[LatestSnap].latestContent.get.tags.exists(_.sectionId == Some("technology")) should be (true)
        })
    }

    "Should request a mix of both" in {
      val normalTrailThree = Trail("internal-code/content/454695023", 0, None)
      val dreamSnapOne = makeLatestTrailFor("snap/1281727", "uk/culture")
      val dreamSnapTwo = makeLatestTrailFor("snap/2372382", "technology")
      val collectionJson = makeCollectionJsonWithTreats(List(dreamSnapOne, normalTrail, dreamSnapTwo, normalTrailTwo), normalTrailThree)
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
      val faciaContent = FAPI.draftCollectionContentWithSnaps(collection, adjustSnapItemQuery = itemQuery => itemQuery.showTags("all"))
      val faciaContentLive = FAPI.liveCollectionContentWithoutSnaps(collection, adjustSearchQuery = searchQuery => searchQuery.showTags("all"))

      faciaContent.asFuture.futureValue.fold(
        err => fail(s"expected 2 treat result, got $err", err.cause),
        contents => {
          contents.size should be(4)
          contents.head.asInstanceOf[LatestSnap].latestContent.get.tags.exists(_.sectionId == Some("culture")) should be (true)
          contents.apply(1).asInstanceOf[CuratedContent].headline should be ("PM returns from holiday after video shows US reporter beheaded by Briton")
          contents.apply(2).asInstanceOf[LatestSnap].latestContent.get.tags.exists(_.sectionId == Some("technology")) should be (true)
          contents.apply(3).asInstanceOf[CuratedContent].headline should be ("Inside the 29 August edition")
        })

      faciaContentLive.asFuture.futureValue.fold(
        err => fail(s"expected 2 treat result, got $err", err.cause),
        contents => {
          contents.size should be(1)
          contents.head.asInstanceOf[CuratedContent].headline should be ("Pope Francis greeted by huge crowds in the Philippines – video")
        })
    }
  }


  def fail(message: String, cause: Option[Throwable]): Nothing = {
    cause.map(fail(message, _)).getOrElse(fail(message))
  }
}
