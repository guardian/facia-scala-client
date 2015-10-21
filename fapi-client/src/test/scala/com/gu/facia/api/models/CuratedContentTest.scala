package com.gu.facia.api.models

import com.gu.contentapi.client.model.{Tag, Content}
import com.gu.facia.api.utils.{TagKicker, SectionKicker}
import com.gu.facia.client.models.{CollectionConfigJson, TrailMetaData}
import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.json.{JsBoolean, JsString}
import org.scalatest.OptionValues._

class CuratedContentTest extends FreeSpec with Matchers {

  "CuratedContent headline" - {
    val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

    val contentWithFieldHeadline = Content(
      "content-id", Some("section"), Some("Section Name"), None, "webTitle", "webUrl", "apiUrl",
      fields = Some(Map("internalContentCode" -> "CODE", "headline" -> "Content headline", "href" -> "Content href", "trailText" -> "Content trailtext", "byline" -> "Content byline")),
      Nil, None, Nil, None, None, None)

    val contentWithoutFieldHeadline = Content(
      "content-id", Some("section"), Some("Section Name"), None, "contentWithoutFieldHeadlineHeadline", "webUrl", "apiUrl",
      fields = Some(Map("internalContentCode" -> "CODE", "href" -> "Content href", "trailText" -> "Content trailtext", "byline" -> "Content byline")),
      Nil, None, Nil, None, None, None)

    val trailMetaDataWithHeadline = TrailMetaData(Map("headline" -> JsString("trailMetaDataHeadline")))
    val trailMetaDataWithoutHeadline = TrailMetaData(Map.empty)

    "should resolve the headline from TrailMetaData" in {
      val curatedContent = CuratedContent.fromTrailAndContent(contentWithFieldHeadline, trailMetaDataWithHeadline, None, collectionConfig)
      curatedContent.headline should be ("trailMetaDataHeadline")
    }

    "should resolve the headline from Content fields.headline" in {
      val curatedContent = CuratedContent.fromTrailAndContent(contentWithFieldHeadline, trailMetaDataWithoutHeadline, None, collectionConfig)
      curatedContent.headline should be ("Content headline")
    }
    "should resolve the headline from Content webTitle" in {
      val curatedContent = CuratedContent.fromTrailAndContent(contentWithoutFieldHeadline, trailMetaDataWithoutHeadline, None, collectionConfig)
      curatedContent.headline should be ("contentWithoutFieldHeadlineHeadline")
    }
  }

  "CuratedContent itemKicker" - {
    val emptyContent = Content(
      "content-id", Some("section"), Some("Section Name"), None, "webTitle", "webUrl", "apiUrl",
      fields = Some(Map("internalContentCode" -> "CODE", "headline" -> "Content headline", "href" -> "Content href", "trailText" -> "Content trailtext", "byline" -> "Content byline")),
      List(Tag("id", "type", None, None, "", "", "")), None, Nil, None, None, None)
    val emptyTrailMetaData = TrailMetaData(Map.empty)
    val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())
    val collectionConfigShowSections = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults(showSections = Option(true)))
    val collectionConfigShowTags = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults(showTags = Option(true)))

    "should resolve to None" in {
      val curatedContent = CuratedContent.fromTrailAndContent(emptyContent, emptyTrailMetaData, None, collectionConfig)
      curatedContent.kicker should be (None)
    }

    "should resolve to SectionKicker with config showSections true" in {
      val curatedContent = CuratedContent.fromTrailAndContent(emptyContent, emptyTrailMetaData, None, collectionConfigShowSections)
      curatedContent.kicker.value shouldBe a [SectionKicker]
    }

    "should resolve to TagKicker with config showTags true" in {
      //This test requires content to have tags
      val curatedContent = CuratedContent.fromTrailAndContent(emptyContent, emptyTrailMetaData, None, collectionConfigShowTags)
      curatedContent.kicker.value shouldBe a [TagKicker]
    }

    "should resolve to SectionKicker for trailMetaData showKickerSection true" in {
      val trailMetaDataShowKickerSection = TrailMetaData(Map("showKickerSection" -> JsBoolean(value = true)))
      val curatedContent = CuratedContent.fromTrailAndContent(emptyContent, trailMetaDataShowKickerSection, None, collectionConfigShowTags)
      curatedContent.kicker.value shouldBe a [SectionKicker]
    }

    "should resolve to SectionKicker for trailMetaData showKickerTag true" in {
      val trailMetaDataShowKickerTag = TrailMetaData(Map("showKickerTag" -> JsBoolean(value = true)))
      val curatedContent = CuratedContent.fromTrailAndContent(emptyContent, trailMetaDataShowKickerTag, None, collectionConfigShowTags)
      curatedContent.kicker.value shouldBe a [TagKicker]
    }
  }

  "SupportingCuratedContent itemKicker" - {
    val emptyContent = Content(
      "content-id", Some("section"), Some("Section Name"), None, "webTitle", "webUrl", "apiUrl",
      fields = Some(Map("internalContentCode" -> "CODE", "headline" -> "Content headline", "href" -> "Content href", "trailText" -> "Content trailtext", "byline" -> "Content byline")),
      List(Tag("id", "type", None, None, "", "", "")), None, Nil, None, None, None)
    val emptyTrailMetaData = TrailMetaData(Map.empty)
    val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())
    val collectionConfigShowSections = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults(showSections = Option(true)))
    val collectionConfigShowTags = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults(showTags = Option(true)))

    "should resolve to None" in {
      val supportingCuratedContent = SupportingCuratedContent.fromTrailAndContent(emptyContent, emptyTrailMetaData, None, collectionConfig)
      supportingCuratedContent.kicker should be (None)
    }

    "should resolve to None with config showSections true" in {
      val supportingCuratedContent = SupportingCuratedContent.fromTrailAndContent(emptyContent, emptyTrailMetaData, None, collectionConfigShowSections)
      supportingCuratedContent.kicker should be (None)
    }

    "should resolve to None with config showTags true" in {
      val supportingCuratedContent = SupportingCuratedContent.fromTrailAndContent(emptyContent, emptyTrailMetaData, None, collectionConfigShowTags)
      supportingCuratedContent.kicker should be (None)
    }

    "should resolve to SectionKicker for trailMetaData showKickerSection true" in {
      val trailMetaDataShowKickerSection = TrailMetaData(Map("showKickerSection" -> JsBoolean(value = true)))
      val supportingCuratedContent = SupportingCuratedContent.fromTrailAndContent(emptyContent, trailMetaDataShowKickerSection, None, collectionConfigShowTags)
      supportingCuratedContent.kicker.value shouldBe a [SectionKicker]
    }

    "should resolve to SectionKicker for trailMetaData showKickerTag true" in {
      val trailMetaDataShowKickerTag = TrailMetaData(Map("showKickerTag" -> JsBoolean(value = true)))
      val supportingCuratedContent = SupportingCuratedContent.fromTrailAndContent(emptyContent, trailMetaDataShowKickerTag, None, collectionConfigShowTags)
      supportingCuratedContent.kicker.value shouldBe a [TagKicker]
    }
  }
}
