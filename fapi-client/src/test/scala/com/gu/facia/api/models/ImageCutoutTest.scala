package com.gu.facia.api.models

import com.gu.contentapi.client.model.{Tag, Content}
import com.gu.facia.client.models.TrailMetaData
import org.scalatest.{Matchers, FreeSpec}
import play.api.libs.json.{JsString, JsBoolean}

class ImageCutoutTest extends FreeSpec with Matchers {

  def trailMetaDataWithImageCutout(
    imageCutoutReplace: Boolean = false,
    imageCutoutSrc: Option[String] = None,
    imageCutoutSrcWidth: Option[String] = None,
    imageCutoutSrcHeight: Option[String] = None) = TrailMetaData(Map(
    "imageCutoutReplace" -> JsBoolean(imageCutoutReplace))
    ++ imageCutoutSrc.map("imageCutoutSrc" -> JsString(_))
    ++ imageCutoutSrcWidth.map("imageCutoutSrcWidth" -> JsString(_))
    ++ imageCutoutSrcHeight.map("imageCutoutSrcHeight" -> JsString(_)))

  val emptyContent =
    Content(
      "id", None, None, None,
      "webTitle", "webUrl", "apiUrl", None,
      Nil, None, Nil, None)

  def contentWithContributorTag(bylineLargeImageUrl: String): Content =
    emptyContent.copy(tags = List(Tag(
      "tagid", "contributor", None, None, "webTitle",
      "webUrl", "apiUrl", Nil, None, None, bylineImageUrl = None,
      Option(bylineLargeImageUrl), None, None, None, None)))

  val bylineImageUrl = "http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2014/3/13/1394733744420/MichaelWhite.png"
  val contentWithContributor = contentWithContributorTag(bylineImageUrl)

  "ImageCutout" - {

    "should return empty ImageCutout" in {
      ImageCutout.empty should be (ImageCutout(false, None, None, None))
    }

    "should return true for standalone true" in {
      val trailMeta = trailMetaDataWithImageCutout(true)
      val imageCutout = ImageCutout.fromContentAndTrailMeta(emptyContent, trailMeta)
      imageCutout.imageCutoutReplace should be (true)
    }

    "should return src with replace true or false" in {
      val src = Option("src")
      val width = Option("width")
      val height = Option("height")

      val trailMetaTrue = trailMetaDataWithImageCutout(true, src, width, height)
      val imageCutoutTrue = ImageCutout.fromContentAndTrailMeta(emptyContent, trailMetaTrue)
      imageCutoutTrue.imageCutoutReplace should be (true)
      imageCutoutTrue.imageCutoutSrc should be (src)
      imageCutoutTrue.imageCutoutSrcWidth should be (width)
      imageCutoutTrue.imageCutoutSrcHeight should be (height)

      val trailMetaFalse = trailMetaDataWithImageCutout(false, src, width, height)
      val imageCutoutFalse = ImageCutout.fromContentAndTrailMeta(emptyContent, trailMetaFalse)
      imageCutoutFalse.imageCutoutReplace should be (false)
      imageCutoutFalse.imageCutoutSrc should be (src)
      imageCutoutFalse.imageCutoutSrcWidth should be (width)
      imageCutoutFalse.imageCutoutSrcHeight should be (height)
    }

    "should return None for src, width, height if any are empty" in {
      val src = Option("src")
      val widthNone: Option[String] = None
      val height = Option("height")

      val trailMeta = trailMetaDataWithImageCutout(true, src, widthNone, height)
      val imageCutout = ImageCutout.fromContentAndTrailMeta(emptyContent, trailMeta)
      imageCutout.imageCutoutReplace should be (true)
      imageCutout.imageCutoutSrc should be (None)
      imageCutout.imageCutoutSrcWidth should be (None)
      imageCutout.imageCutoutSrcHeight should be (None)
    }

    "should return an ImageCutout from content tags" in {
      val trailMeta = trailMetaDataWithImageCutout(true, None, None, None)
      val imageCutout = ImageCutout.fromContentAndTrailMeta(contentWithContributor, trailMeta)
      imageCutout.imageCutoutReplace should be (true)
      imageCutout.imageCutoutSrc should be (Some(bylineImageUrl))
      imageCutout.imageCutoutSrcWidth should be (None)
      imageCutout.imageCutoutSrcHeight should be (None)
    }

    "should not return an ImageCutout from content tags if imageCutoutReplace is false" in {
      val trailMeta = trailMetaDataWithImageCutout(false, None, None, None)
      val imageCutout = ImageCutout.fromContentAndTrailMeta(contentWithContributor, trailMeta)
      imageCutout.imageCutoutReplace should be (false)
      imageCutout.imageCutoutSrc should be (None)
      imageCutout.imageCutoutSrcWidth should be (None)
      imageCutout.imageCutoutSrcHeight should be (None)
    }
  }

}
