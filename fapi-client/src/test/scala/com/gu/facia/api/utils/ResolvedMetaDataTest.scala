package com.gu.facia.api.utils

import com.gu.contentapi.client.model.{Content, Tag}
import com.gu.facia.client.models.TrailMetaData
import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.json.JsBoolean

class ResolvedMetaDataTest extends FreeSpec with Matchers {

  "Resolving Metadata using fromContent" - {
    def tagWithId(id: String) = Tag(
      id = id,
      `type` = "",
      webTitle = "",
      webUrl = "",
      apiUrl = "")

    def contentWithTags(tags: Tag*): Content = Content(
      "content-id", Some("section"), Some("Section Name"), None, "webTitle", "webUrl", "apiUrl",
      fields = Option(Map.empty), tags.toList, None, Nil, None)

    "Content with type cartoon should showByline" in {
      val contentWithCartoon = contentWithTags(tagWithId("type/cartoon"))
      ResolvedMetaData.fromContent(contentWithCartoon).showByline should be (true)
    }

    "Content with type comment should showByline, showQuotedHeadline and imageCutoutReplace" in {
      val contentWithComment = contentWithTags(tagWithId("tone/comment"))
      ResolvedMetaData.fromContent(contentWithComment).showByline should be (true)
      ResolvedMetaData.fromContent(contentWithComment).showQuotedHeadline should be (true)
      ResolvedMetaData.fromContent(contentWithComment).imageCutoutReplace should be (true)
    }

    "Content with type video should showMainVideo" in {
      val contentWithVideo = contentWithTags(tagWithId("type/video"))
      ResolvedMetaData.fromContent(contentWithVideo).showMainVideo should be (true)
    }

    "Content with silly type should all false" in {
      val contentWithVideo = contentWithTags(tagWithId("sillyid"))
      val resolvedMetaData = ResolvedMetaData.fromContent(contentWithVideo)
     resolvedMetaData.showByline should be (false)
     resolvedMetaData.showQuotedHeadline should be (false)
     resolvedMetaData.imageCutoutReplace should be (false)
     resolvedMetaData.showMainVideo should be (false)
     resolvedMetaData.isBoosted should be (false)
     resolvedMetaData.isBreaking should be (false)
     resolvedMetaData.imageHide should be (false)
     resolvedMetaData.imageReplace should be (false)
     resolvedMetaData.showKickerCustom should be (false)
     resolvedMetaData.showKickerSection should be (false)
     resolvedMetaData.showKickerTag should be (false)
    }
  }

  "Resolving Metadata using fromTrailMetaData" - {

    "Resolve all to false for empty TrailMetaData" in {
      val emptyTrailMetaData = TrailMetaData(Map.empty)
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(emptyTrailMetaData)
      resolvedMetaData.showByline should be (false)
      resolvedMetaData.showQuotedHeadline should be (false)
      resolvedMetaData.imageCutoutReplace should be (false)
      resolvedMetaData.showMainVideo should be (false)
      resolvedMetaData.isBoosted should be (false)
      resolvedMetaData.isBreaking should be (false)
      resolvedMetaData.imageHide should be (false)
      resolvedMetaData.imageReplace should be (false)
      resolvedMetaData.showKickerCustom should be (false)
      resolvedMetaData.showKickerSection should be (false)
      resolvedMetaData.showKickerTag should be (false)
    }

    "Resolve all to true for empty TrailMetaData" in {
      val trailMetaData = TrailMetaData(
        Map("showByline" -> JsBoolean(true),
        "showMainVideo" -> JsBoolean(true),
        "showQuotedHeadline" -> JsBoolean(true),
        "imageCutoutReplace" -> JsBoolean(true)))
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(trailMetaData)
      resolvedMetaData.showByline should be (true)
      resolvedMetaData.showQuotedHeadline should be (true)
      resolvedMetaData.imageCutoutReplace should be (true)
      resolvedMetaData.showMainVideo should be (true)
      resolvedMetaData.isBoosted should be (false)
      resolvedMetaData.isBreaking should be (false)
      resolvedMetaData.imageHide should be (false)
      resolvedMetaData.imageReplace should be (false)
      resolvedMetaData.showKickerCustom should be (false)
      resolvedMetaData.showKickerSection should be (false)
      resolvedMetaData.showKickerTag should be (false)
    }

  }

}
