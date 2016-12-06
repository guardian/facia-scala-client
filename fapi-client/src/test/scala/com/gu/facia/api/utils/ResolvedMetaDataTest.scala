package com.gu.facia.api.utils

import com.gu.contentapi.client.model.v1._
import com.gu.facia.client.models.TrailMetaData
import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.json.JsBoolean
import lib.TestContent

class ResolvedMetaDataTest extends FreeSpec with Matchers with TestContent {

  val contentWithCartoon = contentWithTags(tagWithId("type/cartoon"))
  val contentWithComment = contentWithTags(tagWithId("tone/comment"))
  val contentWithVideo = contentWithTags(tagWithId("type/video"))
  val contentWithAtom: Content = contentWithVideo.copy(blocks = Some(Blocks(main = Some(Block(id = "foo", bodyHtml = "foo", bodyTextSummary = "foo", title = None, attributes = BlockAttributes(), published = true, elements = Seq(BlockElement(`type` = ElementType.Contentatom)))))))
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

  def tagWithId(id: String) = Tag(
    id = id,
    `type` = TagType.Keyword,
    webTitle = "",
    webUrl = "",
    apiUrl = "")

  def contentWithTags(tags: Tag*): Content = baseContent.copy(tags = tags.toList)

  "Resolving Metadata using fromContent" - {

    "Content with type cartoon should showByline" in {
      val resolvedMetaData = ResolvedMetaData.fromContent(contentWithCartoon, DefaultCardstyle)
      resolvedMetaData should have(
        'showByline (true))
    }

    "Content with type comment should showByline, showQuotedHeadline and imageCutoutReplace" in {
      val resolvedMetaData = ResolvedMetaData.fromContent(contentWithComment, Comment)
      resolvedMetaData should have(
        'showByline (true),
        'showQuotedHeadline (true),
        'imageCutoutReplace (true))
    }

    "Content with type video should showMainVideo" in {
      val resolvedMetaData = ResolvedMetaData.fromContent(contentWithVideo, DefaultCardstyle)
      resolvedMetaData should have(
        'showMainVideo (true))
    }

    "Content with silly type should all false" in {
      val contentWithVideo = contentWithTags(tagWithId("sillyid"))
      val resolvedMetaData = ResolvedMetaData.fromContent(contentWithVideo, DefaultCardstyle)
      resolvedMetaData should have(
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
      resolvedMetaData should have(
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
      resolvedMetaData should have(
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
      val resolvedCartoon = ResolvedMetaData.fromContentAndTrailMetaData(contentWithCartoon, emptyTrailMetaData, DefaultCardstyle)
      resolvedCartoon should have(
        'showByline (true))
    }

    "should resolve correct for comment when trailMetaData is not set" in {
      val resolvedComment = ResolvedMetaData.fromContentAndTrailMetaData(contentWithComment, emptyTrailMetaData, Comment)
      resolvedComment should have(
        'showByline (true),
        'showQuotedHeadline (true),
        'imageCutoutReplace (true))
    }

    "should resolve correct for video when trailMetaData is not set" in {
      val resolvedVideo = ResolvedMetaData.fromContentAndTrailMetaData(contentWithVideo, emptyTrailMetaData, DefaultCardstyle)
      resolvedVideo should have(
        'showMainVideo (true))
    }

    "should resolve correct for video with Atom" in {
      val resolvedVideo = ResolvedMetaData.fromContentAndTrailMetaData(contentWithAtom, emptyTrailMetaData, DefaultCardstyle)
      resolvedVideo should have(
        'showMainVideo (false))
    }

    "should resolve correct for cartoon when trailMetaData IS set" in {
      val resolvedCartoon = ResolvedMetaData.fromContentAndTrailMetaData(contentWithCartoon, trailMetaDataWithFieldsSetFalse, DefaultCardstyle)
      resolvedCartoon should have(
        'showByline (false))
    }

    "should resolve correct for comment when trailMetaData IS set" in {
      val resolvedComment = ResolvedMetaData.fromContentAndTrailMetaData(contentWithComment, trailMetaDataWithFieldsSetFalse, DefaultCardstyle)
      resolvedComment should have(
        'showByline (false),
        'showQuotedHeadline (false),
        'imageCutoutReplace (false))
    }

    "should resolve correct for video when trailMetaData IS set" in {
      val resolvedVideo = ResolvedMetaData.fromContentAndTrailMetaData(contentWithVideo, trailMetaDataWithFieldsSetFalse, DefaultCardstyle)
      resolvedVideo should have(
        'showMainVideo (false))
    }
  }

}
