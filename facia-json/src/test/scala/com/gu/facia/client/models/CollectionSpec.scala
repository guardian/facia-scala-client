package com.gu.facia.client.models

import org.specs2.mutable.Specification
import play.api.libs.json.{JsString, JsSuccess, JsError, Json}
import com.gu.facia.client.lib.ResourcesHelper
import org.joda.time.{DateTimeZone, DateTime}

class CollectionSpec extends Specification with ResourcesHelper {
  def getCollectionFixture(path: String) = Json.fromJson[CollectionJson](
    Json.parse(slurpOrDie(path).get)
  ) match {
    case JsSuccess(a, _) => a
    case e @ JsError(_) => println(e)
      throw new RuntimeException("ARGH")
  }

  "Collection" should {
    "deserialize" in {
      val collection = getCollectionFixture("DEV/frontsapi/collection/2409-31b3-83df0-de5a/collection.json")

      collection.live must haveLength(8)

      collection.live.find(_.id == "football/quiz/2014/jun/11/world-cup-2014-the-ultimate-world-cup-trivia-quiz") must
        beSome.which({ front =>
          (front.frontPublicationDate mustEqual 1402500092818l) and (front.meta mustEqual Some(TrailMetaData.withDefaults(
            ("headline", JsString("The ultimate World Cup trivia quiz")),
            ("group", JsString("0"))
          )))
        })

      collection.lastUpdated mustEqual new DateTime(2014, 6, 12, 8, 30, 20, 67, DateTimeZone.UTC)

      collection.updatedBy mustEqual "Katherine Le Ruez"

      collection.updatedEmail mustEqual "katherine.leruez@guardian.co.uk"
    }

    "deserialize content with image src widths and heights attached" in {
      val collection = getCollectionFixture("PROD/frontsapi/collection/uk-alpha/news/regular-stories/collection2.json")

      collection.live.lift(1) must beSome.which({ item =>
        item.safeMeta.imageSrcWidth mustEqual Some("940") and (item.safeMeta.imageSrcHeight mustEqual Some("564"))
      })
    }

    "deserialize content without metadata" in {
      val collection = getCollectionFixture("PROD/frontsapi/collection/754c-8e8c-fad9-a927/collection.json")

      collection.live.lift(2) must beSome.which({ item =>
        item.meta mustEqual None
      })
    }

    "deserialize content with supporting items" in {
      val collection = getCollectionFixture("PROD/frontsapi/collection/uk-alpha/news/regular-stories/collection.json")

      collection.live.headOption must beSome.which({ item =>
        item.safeMeta.supporting must beSome.which({ supportingContent =>
          supportingContent must haveLength(3)

          supportingContent.lift(1) must beSome.which({ item =>
            item.id mustEqual "internal-code/content/442568601"

            item.meta must beSome.which({ meta =>
              meta.group mustEqual Some("2")

              meta.headline mustEqual Some("Special report: Scores killed in deadliest assault in Gaza so far")
            })
          })
        })
      })

      "be able to serialize and deserialize the same object" in {
        val collectionJson = CollectionJson(
          live = List(Trail("id-123", DateTime.now.getMillis, None)),
          draft = None,
          treats = None,
          lastUpdated = DateTime.now(),
          updatedBy = "A test",
          updatedEmail = "a test email",
          displayName = None,
          href = None,
          previously = None,
          description = Some("desc"))

        val collectionJsonAsString = Json.stringify(Json.toJson(collectionJson))
        val newCollectionJson = Json.parse(collectionJsonAsString).as[CollectionJson]

        newCollectionJson.lastUpdated should beEqualTo (collectionJson.lastUpdated)
        newCollectionJson should beEqualTo (collectionJson)
      }
    }
  }
}
