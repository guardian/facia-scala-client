package com.gu.facia.client.models

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import com.gu.facia.client.lib.ResourcesHelper

class ConfigSpec extends Specification with ResourcesHelper {
  "Config" should {
    "deserialize" in {
      val config = Json.fromJson[Config](Json.parse(slurpOrDie("frontsapi/config/config.json"))).get

      config.collections.get("uk/commentisfree/most-viewed/regular-stories") must beSome.which({ collection =>
        (collection.displayName must beSome.which(_ == "Most popular")) and
          (collection.`type` must beSome.which(_ == "news/most-popular")) and
          (collection.uneditable must beSome.which(identity)) and
          (collection.apiQuery must beSome.which(_ ==
            "uk/commentisfree?show-most-viewed=true&show-editors-picks=false&hide-recent-content=true"))
      })

      config.fronts.get("lifeandstyle/home-and-garden") must beSome.which({ front =>
        (front.collections must haveLength(2)) and
          (front.title must beSome.which(_ == "Home, interiors and gardening news, comment and advice")) and
          (front.description must beSome.which(_ == "Latest news, comment and advice on homes, interior design, " +
          "decorating and gardening from the Guardian, the world's leading liberal voice"))
      })
    }
  }
}
