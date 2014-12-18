package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.client.models.{TrailMetaData, Trail}
import play.api.libs.json.JsValue

case class Image(imageSrc: String, imageSrcWidth: String, imageSrcHeight: String)

object Image {
  def fromTrail(trail: Trail): Option[Image] =
    for {
      imageSrc <- trail.safeMeta.imageSrc
      imageSrcWidth <- trail.safeMeta.imageSrcWidth
      imageSrcHeight <- trail.safeMeta.imageSrcHeight
    } yield Image(imageSrc, imageSrcWidth, imageSrcHeight)
}

sealed trait Kicker

sealed trait ImageCutout

sealed trait FaciaContent
object Snap extends FaciaContent
object SnapLatest extends FaciaContent
case class CuratedContent(
  content: Content,
  headline: String,
  href: String,
  trailText: String,
  group: String,
  image: Option[Image],
  isBreaking: Boolean,
  isBoosted: Boolean,
  imageHide: Boolean,
  imageReplace: Boolean,
  showMainVideo: Boolean,
  showKickerTag: Boolean,
  byline: String,
  showByLine: Boolean,
  kicker: Option[Kicker],
  imageCutout: Option[ImageCutout],
  showBoostedHeadline: Boolean,
  showQuotedHeadline: Boolean) extends FaciaContent

object FaciaContent {

  def fromTrailAndContent(trail: Trail, content: Content): CuratedContent = {
    val trailMetaData: TrailMetaData = trail.safeMeta
    val contentFields: Map[String, String] = content.safeFields

    CuratedContent(
      content,
      trailMetaData.headline.orElse(contentFields.get("headline")).get,
      trailMetaData.href.orElse(contentFields.get("href")).get,
      trailMetaData.trailText.orElse(contentFields.get("trailText")).get,
      trailMetaData.group.orElse(contentFields.get("group")).get,
      Image.fromTrail(trail),
      trailMetaData.isBreaking.getOrElse(false),
      trailMetaData.isBoosted.getOrElse(false),
      trailMetaData.imageHide.getOrElse(false),
      trailMetaData.imageReplace.getOrElse(false),
      trailMetaData.showMainVideo.getOrElse(false),
      trailMetaData.showKickerTag.getOrElse(false),
      trailMetaData.byline.orElse(contentFields.get("byline")).get,
      trailMetaData.showByline.getOrElse(false),
      None,
      None,
      trailMetaData.showBoostedHeadline.getOrElse(false),
      trailMetaData.showQuotedHeadline.getOrElse(false)
    )
  }
}