package com.gu.facia.api.models

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

  "ImageCutout" - {

    "should return empty ImageCutout" in {
      ImageCutout.empty should be (ImageCutout(false, None, None, None))
    }

    "should return true for standalone true" in {
      val trailMeta = trailMetaDataWithImageCutout(true)
      val imageCutout = ImageCutout.fromTrail(trailMeta)
      imageCutout.imageCutoutReplace should be (true)
    }

    "should return src with replace true or false" in {
      val src = Option("src")
      val width = Option("width")
      val height = Option("height")

      val trailMetaTrue = trailMetaDataWithImageCutout(true, src, width, height)
      val imageCutoutTrue = ImageCutout.fromTrail(trailMetaTrue)
      imageCutoutTrue.imageCutoutReplace should be (true)
      imageCutoutTrue.imageCutoutSrc should be (src)
      imageCutoutTrue.imageCutoutSrcWidth should be (width)
      imageCutoutTrue.imageCutoutSrcHeight should be (height)

      val trailMetaFalse = trailMetaDataWithImageCutout(false, src, width, height)
      val imageCutoutFalse = ImageCutout.fromTrail(trailMetaFalse)
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
      val imageCutout = ImageCutout.fromTrail(trailMeta)
      imageCutout.imageCutoutReplace should be (true)
      imageCutout.imageCutoutSrc should be (None)
      imageCutout.imageCutoutSrcWidth should be (None)
      imageCutout.imageCutoutSrcHeight should be (None)
    }
  }

}
