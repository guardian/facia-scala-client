package com.gu.facia.api.models

import com.gu.facia.api.utils.ResolvedMetaData
import com.gu.facia.client.models.TrailMetaData
import org.scalatest.{Matchers, FlatSpec}
import play.api.libs.json.{JsObject, JsArray, JsString, JsBoolean}

class FaciaImageSlideshowTest extends FlatSpec with Matchers {
  val assetOne = JsObject(List(
    "src" -> JsString("theImageSrcOne"),
    "width" -> JsString("theImageSrcWidthOne"),
    "height" -> JsString("theImageSrcHeightOne")))

  val assetTwo = JsObject(List(
    "src" -> JsString("theImageSrcTwo"),
    "width" -> JsString("theImageSrcWidthTwo"),
    "height" -> JsString("theImageSrcHeightTwo")))

  val assetThree = JsObject(List(
    "src" -> JsString("theImageSrcThree"),
    "width" -> JsString("theImageSrcWidthThree"),
    "height" -> JsString("theImageSrcHeightThree")))

  val trailMetaDataWithoutImageSlideshowReplace =
    TrailMetaData(Map(
      "imageSlideshowReplace" -> JsBoolean(value = false),
      "slideshow" -> JsArray(List(assetOne, assetTwo, assetThree))))

  val trailMetaDataWithImageSlideshowReplace =
    TrailMetaData(Map(
      "imageSlideshowReplace" -> JsBoolean(value = true),
      "slideshow" -> JsArray(List(assetOne, assetTwo, assetThree))))

  val trailMetaDataWithImageSlideshowReplaceAndNoAssets =
    TrailMetaData(Map(
      "imageSlideshowReplace" -> JsBoolean(value = true),
      "slideshow" -> JsArray(Nil)))

  "Image" should "give a None if when imageSlideshowReplace not set" in {
    val resolvedMetaData =  ResolvedMetaData.fromTrailMetaData(trailMetaDataWithoutImageSlideshowReplace)
    FaciaImage.getFaciaImage(None, trailMetaDataWithoutImageSlideshowReplace, resolvedMetaData) should be (None)
  }

  it should "give back an ImageGallery when true" in {
    val resolvedMetaData =  ResolvedMetaData.fromTrailMetaData(trailMetaDataWithImageSlideshowReplace)
    FaciaImage.getFaciaImage(None, trailMetaDataWithImageSlideshowReplace, resolvedMetaData) should be (Some(ImageSlideshow(List(Replace("theImageSrcOne", "theImageSrcWidthOne", "theImageSrcHeightOne"), Replace("theImageSrcTwo", "theImageSrcWidthTwo", "theImageSrcHeightTwo"), Replace("theImageSrcThree", "theImageSrcWidthThree", "theImageSrcHeightThree")))))
  }

  it should "give back a None when imageSlideshowReplace true with no assets" in {
    val resolvedMetaData =  ResolvedMetaData.fromTrailMetaData(trailMetaDataWithImageSlideshowReplaceAndNoAssets)
    FaciaImage.getFaciaImage(None, trailMetaDataWithImageSlideshowReplaceAndNoAssets, resolvedMetaData) should be (None)
  }
}

