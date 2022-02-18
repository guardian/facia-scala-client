package com.gu.facia.client.models

import com.gu.facia.client.lib.ResourcesHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class ConfigSpec extends Specification with ResourcesHelper {
  "Config" should {
    "deserialize" in {
      val config = Json.fromJson[ConfigJson](Json.parse(slurpOrDie("DEV/frontsapi/config/config.json").get)).get

      config.collections.get("uk/commentisfree/most-viewed/regular-stories") must beSome.which({ collection =>
        (collection.displayName must beSome.which(_ == "Most popular")) and
          (collection.`type` must beSome.which(_ == "news/most-popular")) and
          (collection.uneditable must beSome.which(identity)) and
          (collection.backfill must beSome.which({ backfill =>
            (backfill.`type` == "capi") and
            (backfill.query == "uk/commentisfree?show-most-viewed=true&show-editors-picks=false&hide-recent-content=true")
          }))
      })

      config.fronts.get("lifeandstyle/home-and-garden") must beSome.which({ front =>
        (front.collections must haveLength(2)) and
          (front.title must beSome.which(_ == "Home, interiors and gardening news, comment and advice")) and
          (front.description must beSome.which(_ == "Latest news, comment and advice on homes, interior design, " +
          "decorating and gardening from the Guardian, the world's leading liberal voice"))
      })
    }
    "deserialize territories" in {
      val config = Json.fromJson[ConfigJson](Json.parse(slurpOrDie("DEV/frontsapi/config/config.json").get)).get

      config.collections.get("au/sport/golf/regular-stories") must beSome.which({ collection =>
        collection.targetedTerritory.get mustEqual(EU27Territory)
      })
    }
    "deserialize unsupported territories as unknown" in {

      val configJson = """{
        |      "displayName" : "Golf",
        |      "backfill" : {
        |        "type" : "capi",
        |        "query" : "sport/golf?edition=au"
        |      },
        |      "type" : "news/special",
        |      "href" : "sport/golf",
        |      "targetedTerritory": "Made-Up-Territory"
        |    }""".stripMargin
      val parsed = Json.fromJson[CollectionConfigJson](Json.parse(configJson))
      parsed.asOpt must beSome.which({ config =>
        config.targetedTerritory must beSome(UnknownTerritory: TargetedTerritory)
      })
    }

    "serialize territories" in {
      val config = Json.fromJson[ConfigJson](Json.parse(slurpOrDie("DEV/frontsapi/config/config.json").get)).get

      config.collections.get("uk/commentisfree/most-viewed/regular-stories") must beSome.which({ collection =>
        val collectionWithTerritory = collection.copy(targetedTerritory = Some(NZTerritory))
        val json = Json.toJson(collectionWithTerritory).toString()
        json mustEqual """{"displayName":"Most popular","backfill":{"type":"capi","query":"uk/commentisfree?show-most-viewed=true&show-editors-picks=false&hide-recent-content=true"},"type":"news/most-popular","uneditable":true,"targetedTerritory":"NZ"}"""
      })
    }
  }
}
