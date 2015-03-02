package com.gu.facia.api.utils

import com.gu.contentapi.client.model.{Content, Tag}
import com.gu.facia.client.models.TrailMetaData
import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.json.JsBoolean

class ResolvedMetaDataTest extends FreeSpec with Matchers {

  def tagWithId(id: String) = Tag(
    id = id,
    `type` = "",
    webTitle = "",
    webUrl = "",
    apiUrl = "")

  def contentWithTags(tags: Tag*): Content = Content(
    "content-id", Some("section"), Some("Section Name"), None, "webTitle", "webUrl", "apiUrl",
    fields = Option(Map.empty), tags.toList, None, Nil, None)

  val contentWithCartoon = contentWithTags(tagWithId("type/cartoon"))
  val contentWithComment = contentWithTags(tagWithId("tone/comment"))
  val contentWithVideo = contentWithTags(tagWithId("type/video"))

  val emptyTrailMetaData = TrailMetaData(Map.empty)

  val trailMetaDataWithFieldsSetTrue = TrailMetaData(
    Map("showByline" -> JsBoolean(true),
      "showMainVideo" -> JsBoolean(true),
      "showQuotedHeadline" -> JsBoolean(true),
      "imageCutoutReplace" -> JsBoolean(true)))

  val trailMetaDataWithFieldsSetFalse = TrailMetaData(
    Map("showByline" -> JsBoolean(false),
      "showMainVideo" -> JsBoolean(false),
      "showQuotedHeadline" -> JsBoolean(false),
      "imageCutoutReplace" -> JsBoolean(false)))

  "Resolving Metadata using fromContent" - {

    "Content with type cartoon should showByline" in {
      ResolvedMetaData.fromContent(contentWithCartoon).showByline should be (true)
    }

    "Content with type comment should showByline, showQuotedHeadline and imageCutoutReplace" in {
      ResolvedMetaData.fromContent(contentWithComment).showByline should be (true)
      ResolvedMetaData.fromContent(contentWithComment).showQuotedHeadline should be (true)
      ResolvedMetaData.fromContent(contentWithComment).imageCutoutReplace should be (true)
    }

    "Content with type video should showMainVideo" in {
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
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(trailMetaDataWithFieldsSetTrue)
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

  "Resolving Metadata using fromContentAndTrailMetaData" - {

    "should resolve correct for cartoon, comment and video when trailMetaData is not set" in {
      val resolvedCartoon = ResolvedMetaData.fromContentAndTrailMetaData(contentWithCartoon, emptyTrailMetaData)
      resolvedCartoon.showByline should be(true)

      val resolvedComment = ResolvedMetaData.fromContentAndTrailMetaData(contentWithComment, emptyTrailMetaData)
      resolvedComment.showByline should be(true)
      resolvedComment.showQuotedHeadline should be(true)
      resolvedComment.imageCutoutReplace should be(true)

      val resolvedVideo = ResolvedMetaData.fromContentAndTrailMetaData(contentWithVideo, emptyTrailMetaData)
      resolvedVideo.showMainVideo should be(true)
    }

    "should resolve correct for cartoon, comment and video when trailMetaData IS set" in {
      val resolvedCartoon = ResolvedMetaData.fromContentAndTrailMetaData(contentWithCartoon, trailMetaDataWithFieldsSetFalse)
      resolvedCartoon.showByline should be(false)

      val resolvedComment = ResolvedMetaData.fromContentAndTrailMetaData(contentWithComment, trailMetaDataWithFieldsSetFalse)
      resolvedComment.showByline should be(false)
      resolvedComment.showQuotedHeadline should be(false)
      resolvedComment.imageCutoutReplace should be(false)

      val resolvedVideo = ResolvedMetaData.fromContentAndTrailMetaData(contentWithVideo, trailMetaDataWithFieldsSetFalse)
      resolvedVideo.showMainVideo should be(false)
    }
  }

}
