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

  "Image" should "give an Image of type default when it is not set" in {
    Image.fromTrailMeta(trailMetaDataWithoutImageReplace) should be (Some(Image("default", "theImageSrc", Some("theImageSrcWidth"), Some("theImageSrcHeight"))))
  }

  it should "give back an Image when true" in {
    Image.fromTrailMeta(trailMetaDataWithImageReplace) should be (Some(Image("replace", "theImageSrc", Some("theImageSrcWidth"), Some("theImageSrcHeight"))))
  }
}
