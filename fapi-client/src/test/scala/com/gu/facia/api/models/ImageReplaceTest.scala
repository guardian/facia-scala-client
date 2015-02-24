package com.gu.facia.api.models

import com.gu.facia.client.models.TrailMetaData
import org.scalatest.{Matchers, FlatSpec}
import play.api.libs.json.{JsString, JsBoolean}

class ImageReplaceTest extends FlatSpec with Matchers {
  val trailMetaDataWithoutImageReplace =
    TrailMetaData(Map(
      "imageReplace" -> JsBoolean(value = false),
      "imageSrc" -> JsString("theImageSrc"),
      "imageSrcWidth" -> JsString("theImageSrcWidth"),
      "imageSrcHeight" -> JsString("theImageSrcHeight")))

  val trailMetaDataWithImageReplace =
    TrailMetaData(Map(
      "imageReplace" -> JsBoolean(value = true),
      "imageSrc" -> JsString("theImageSrc"),
      "imageSrcWidth" -> JsString("theImageSrcWidth"),
      "imageSrcHeight" -> JsString("theImageSrcHeight")))

  "ImageReplace" should "give a None back when it is not set" in {
    ImageReplace.fromTrail(trailMetaDataWithoutImageReplace) should be (None)
  }

  it should "give back an ImageReplace when true" in {
    ImageReplace.fromTrail(trailMetaDataWithImageReplace) should be (Some(ImageReplace("theImageSrc", "theImageSrcWidth", "theImageSrcHeight")))
  }
}
