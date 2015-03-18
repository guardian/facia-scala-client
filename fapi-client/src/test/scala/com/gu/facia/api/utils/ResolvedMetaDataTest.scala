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
      val resolvedMetaData = ResolvedMetaData.fromContent(contentWithCartoon)
      resolvedMetaData should have (
        'showByline (true))
    }

    "Content with type comment should showByline, showQuotedHeadline and imageCutoutReplace" in {
      val resolvedMetaData = ResolvedMetaData.fromContent(contentWithComment)
      resolvedMetaData should have (
        'showByline (true),
        'showQuotedHeadline (true),
        'imageCutoutReplace (true))
    }

    "Content with type video should showMainVideo" in {
      val resolvedMetaData = ResolvedMetaData.fromContent(contentWithVideo)
      resolvedMetaData should have (
        'showMainVideo (true))
    }

    "Content with silly type should all false" in {
      val contentWithVideo = contentWithTags(tagWithId("sillyid"))
      val resolvedMetaData = ResolvedMetaData.fromContent(contentWithVideo)
      resolvedMetaData should have (
        'showByline (false),
        'showQuotedHeadline (false),
        'imageCutoutReplace (false),
        'showMainVideo (false),
        'isBoosted (false),
        'isBreaking (false),
        'imageHide (false),
        'imageReplace (false),
        'showKickerCustom (false),
        'showKickerSection (false),
        'showKickerTag (false))
    }
  }

  "Resolving Metadata using fromTrailMetaData" - {

    "Resolve all to false for empty TrailMetaData" in {
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(emptyTrailMetaData)
      resolvedMetaData should have (
        'showByline (false),
        'showQuotedHeadline (false),
        'imageCutoutReplace (false),
        'showMainVideo (false),
        'isBoosted (false),
        'isBreaking (false),
        'imageHide (false),
        'imageReplace (false),
        'showKickerCustom (false),
        'showKickerSection (false),
        'showKickerTag (false))
    }

    "Resolve all to true for empty TrailMetaData" in {
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(trailMetaDataWithFieldsSetTrue)
      resolvedMetaData should have (
        'showByline (true),
        'showQuotedHeadline (true),
        'imageCutoutReplace (true),
        'showMainVideo (true),
        'isBoosted (false),
        'isBreaking (false),
        'imageHide (false),
        'imageReplace (false),
        'showKickerCustom (false),
        'showKickerSection (false),
        'showKickerTag (false)
      )
    }
  }

  "Resolving Metadata using fromContentAndTrailMetaData" - {

    "should resolve correct for cartoon when trailMetaData is not set" in {
      val resolvedCartoon = ResolvedMetaData.fromContentAndTrailMetaData(contentWithCartoon, emptyTrailMetaData)
      resolvedCartoon should have (
        'showByline (true))
    }

    "should resolve correct for comment when trailMetaData is not set" in {
      val resolvedComment = ResolvedMetaData.fromContentAndTrailMetaData(contentWithComment, emptyTrailMetaData)
      resolvedComment should have (
        'showByline (true),
        'showQuotedHeadline (true),
        'imageCutoutReplace (true))
    }

    "should resolve correct for video when trailMetaData is not set" in {
      val resolvedVideo = ResolvedMetaData.fromContentAndTrailMetaData(contentWithVideo, emptyTrailMetaData)
      resolvedVideo should have (
        'showMainVideo (true))
    }

    "should resolve correct for cartoon when trailMetaData IS set" in {
      val resolvedCartoon = ResolvedMetaData.fromContentAndTrailMetaData(contentWithCartoon, trailMetaDataWithFieldsSetFalse)
      resolvedCartoon should have (
        'showByline (false))
    }

    "should resolve correct for comment when trailMetaData IS set" in {
      val resolvedComment = ResolvedMetaData.fromContentAndTrailMetaData(contentWithComment, trailMetaDataWithFieldsSetFalse)
      resolvedComment should have (
        'showByline (false),
        'showQuotedHeadline (false),
        'imageCutoutReplace (false))
    }

    "should resolve correct for video when trailMetaData IS set" in {
      val resolvedVideo = ResolvedMetaData.fromContentAndTrailMetaData(contentWithVideo, trailMetaDataWithFieldsSetFalse)
      resolvedVideo should have (
        'showMainVideo (false))
    }
  }

}