package com.gu.facia.api.models

import com.gu.contentapi.client.model.v1.{TagType, ContentFields, Tag, Content}
import com.gu.facia.api.utils.ResolvedMetaData
import com.gu.facia.client.models.TrailMetaData
import lib.TestContent
import org.scalatest.{Matchers, FreeSpec}
import play.api.libs.json.{JsString, JsBoolean}

class FaciaImageCutoutTest extends FreeSpec with Matchers {

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

    "should return None for imageCutoutReplace=true but nothing to replace with" in {
      val trailMeta = trailMetaDataWithImageCutout(true)
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(trailMeta)
      val imageCutout = FaciaImage.getFaciaImage(Some(emptyContent), trailMeta, resolvedMetaData)
      imageCutout should be (None)
    }

    "should return src with replace true or false" in {
      val src    = "src"
      val width  = Option("width")
      val height = Option("height")

      val trailMetaTrue = trailMetaDataWithImageCutout(true, Option(src), width, height)
      val resolvedMetaDataTrue = ResolvedMetaData.fromTrailMetaData(trailMetaTrue)
      val imageCutoutTrue = FaciaImage.getFaciaImage(Some(emptyContent), trailMetaTrue, resolvedMetaDataTrue)
      imageCutoutTrue.isDefined should be (true)
      imageCutoutTrue should be (Some(Cutout(src, width, height)))

      val trailMetaFalse = trailMetaDataWithImageCutout(false, Option(src), width, height)
      val resolvedMetaDataFalse = ResolvedMetaData.fromTrailMetaData(trailMetaFalse)
      val imageCutoutFalse = FaciaImage.getFaciaImage(Some(emptyContent), trailMetaFalse, resolvedMetaDataFalse)
      imageCutoutFalse should be (None)
    }

    "should return None for src, width, height if all are empty" in {
      val src = Option("src")
      val widthNone: Option[String] = None
      val height = Option("height")

      val trailMeta = trailMetaDataWithImageCutout(true, src, widthNone, height)
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(trailMeta)
      val imageCutout = FaciaImage.getFaciaImage(Some(emptyContent), trailMeta, resolvedMetaData)
      imageCutout should be (None)
    }

    "should return an Image of type cutout from content tags" in {
      val trailMeta = trailMetaDataWithImageCutout(true, None, None, None)
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(trailMeta)
      val imageCutout = FaciaImage.getFaciaImage(Some(contentWithContributor), trailMeta, resolvedMetaData)
      imageCutout.isDefined should be (true)
      imageCutout should be (Some(Cutout(bylineImageUrl, None, None)))
    }

    "should not return an Image of type cutout from content tags if imageCutoutReplace is false" in {
      val trailMeta = trailMetaDataWithImageCutout(false, None, None, None)
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(trailMeta)
      val imageCutout = FaciaImage.getFaciaImage(Some(contentWithContributor), trailMeta, resolvedMetaData)
      imageCutout should be (None)
    }

    "should not return an Image of type cutout from a None content imageCutoutReplace is true" in {
      val src = Option("src")
      val width  = Option("width")
      val height = Option("height")

      val trailMeta = trailMetaDataWithImageCutout(true, src, width, height)
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(trailMeta)
      val imageCutout = FaciaImage.getFaciaImage(None, trailMeta, resolvedMetaData)
      imageCutout should be (Some(Cutout("src", width, height)))
    }
  }

}
