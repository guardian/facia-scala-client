package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.client.models.{CollectionConfigJson, TrailMetaData}
import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.json.JsString

class CuratedContentTest extends FreeSpec with Matchers {

  "CuratedContent headline" - {
    val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

    val contentWithFieldHeadline = Content(
      "content-id", Some("section"), Some("Section Name"), None, "webTitle", "webUrl", "apiUrl",
      fields = Some(Map("internalContentCode" -> "CODE", "headline" -> "Content headline", "href" -> "Content href", "trailText" -> "Content trailtext", "byline" -> "Content byline")),
      Nil, None, Nil, None)

    val contentWithoutFieldHeadline = Content(
      "content-id", Some("section"), Some("Section Name"), None, "contentWithoutFieldHeadlineHeadline", "webUrl", "apiUrl",
      fields = Some(Map("internalContentCode" -> "CODE", "href" -> "Content href", "trailText" -> "Content trailtext", "byline" -> "Content byline")),
      Nil, None, Nil, None)

    val trailMetaDataWithHeadline = TrailMetaData(Map("headline" -> JsString("trailMetaDataHeadline")))
    val trailMetaDataWithoutHeadline = TrailMetaData(Map.empty)

    "should resolve the headline from TrailMetaData" in {
      val curatedContent = FaciaContent.fromTrailAndContent(contentWithFieldHeadline, trailMetaDataWithHeadline, collectionConfig)
      curatedContent.headline should be ("trailMetaDataHeadline")
    }

    "should resolve the headline from Content fields.headline" in {
      val curatedContent = FaciaContent.fromTrailAndContent(contentWithFieldHeadline, trailMetaDataWithoutHeadline, collectionConfig)
      curatedContent.headline should be ("Content headline")
    }
    "should resolve the headline from Content webTitle" in {
      val curatedContent = FaciaContent.fromTrailAndContent(contentWithoutFieldHeadline, trailMetaDataWithoutHeadline, collectionConfig)
      curatedContent.headline should be ("contentWithoutFieldHeadlineHeadline")
    }
  }
}
