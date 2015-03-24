package com.gu.facia.api.models

import com.gu.facia.client.models.TrailMetaData
import org.scalatest.{Matchers, FlatSpec}
import play.api.libs.json.{JsString, JsBoolean}

class FaciaImageReplaceTest extends FlatSpec with Matchers {
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
    FaciaImage.fromTrailMeta(trailMetaDataWithoutImageReplace) should be (Some(FaciaImage("default", "theImageSrc", Some("theImageSrcWidth"), Some("theImageSrcHeight"))))
  }

  it should "give back an Image when true" in {
    FaciaImage.fromTrailMeta(trailMetaDataWithImageReplace) should be (Some(FaciaImage("replace", "theImageSrc", Some("theImageSrcWidth"), Some("theImageSrcHeight"))))
  }
}
