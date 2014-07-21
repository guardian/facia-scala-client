package com.gu.facia.client.models

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import com.gu.facia.client.lib.ResourcesHelper
import org.joda.time.{DateTimeZone, DateTime}

class CollectionSpec extends Specification with ResourcesHelper {
  "Collection" should {
    "deserialize" in {
      val collection = Json.fromJson[Collection](
        Json.parse(slurpOrDie("DEV/frontsapi/collection/2409-31b3-83df0-de5a/collection.json"))
      ).get

      collection.live must haveLength(8)

      collection.live.find(_.id == "football/quiz/2014/jun/11/world-cup-2014-the-ultimate-world-cup-trivia-quiz") must
        beSome.which({ front =>
          (front.frontPublicationDate mustEqual 1402500092818l) and (front.meta mustEqual TrailMetaData.empty.copy(
            headline = Some("The ultimate World Cup trivia quiz"),
            group = Some("0")
          ))
        })

      collection.lastUpdated mustEqual new DateTime(2014, 6, 12, 8, 30, 20, 67, DateTimeZone.UTC)

      collection.updatedBy mustEqual "Katherine Le Ruez"

      collection.updatedEmail mustEqual "katherine.leruez@guardian.co.uk"
    }

    "deserialize content with supporting items" in {
      val collection = Json.fromJson[Collection](
        Json.parse(slurpOrDie("PROD/frontsapi/collection/uk-alpha/news/regular-stories/collection.json"))
      ).get

      collection.live.headOption must beSome.which({ item =>
        item.meta.supporting must beSome.which({ supportingContent =>
          supportingContent must haveLength(3)

          supportingContent.lift(1) must beSome.which({ item =>
            item.id mustEqual "internal-code/content/442568601"

            item.meta must beSome.which({ meta =>
              meta.imageAdjust mustEqual Some("boost")

              meta.group mustEqual Some("2")

              meta.headline mustEqual Some("Special report: Scores killed in deadliest assault in Gaza so far")
            })
          })
        })
      })
    }
  }
}
