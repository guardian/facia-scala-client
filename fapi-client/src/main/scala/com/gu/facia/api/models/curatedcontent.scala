package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.ItemKicker
import com.gu.facia.client.models.{Trail, TrailMetaData}

case class Image(imageSrc: String, imageSrcWidth: String, imageSrcHeight: String)

object Image {
  def fromTrail(trailMeta: TrailMetaData): Option[Image] =
    for {
      imageSrc <- trailMeta.imageSrc
      imageSrcWidth <- trailMeta.imageSrcWidth
      imageSrcHeight <- trailMeta.imageSrcHeight
    } yield Image(imageSrc, imageSrcWidth, imageSrcHeight)
}

case class ImageCutout(
  imageCutoutReplace: Boolean,
  imageCutoutSrc: String,
  imageCutoutSrcWidth: String,
  imageCutoutSrcHeight: String)

object ImageCutout {
  def fromTrail(trailMeta: TrailMetaData): Option[ImageCutout] =
    for {
      imageCutoutSrc <- trailMeta.imageCutoutSrc
      imageCutoutSrcWidth <- trailMeta.imageCutoutSrcWidth
      imageCutoutSrcHeight <- trailMeta.imageCutoutSrcHeight
    } yield ImageCutout(trailMeta.imageCutoutReplace.getOrElse(false),
      imageCutoutSrc, imageCutoutSrcWidth, imageCutoutSrcHeight)
}

sealed trait FaciaContent

object Snap {
  def maybeFromTrail(trail: Trail): Option[Snap] = trail.safeMeta.snapType match {
    case Some("link") =>
      Option(LinkSnap(
        trail.id,
        trail.safeMeta.snapUri,
        trail.safeMeta.snapCss))
    case Some("latest") =>
      Option(LatestSnap(
        trail.id,
        trail.safeMeta.snapUri,
        trail.safeMeta.snapCss,
        None
      ))
    case _ => None
  }
}

sealed trait Snap extends FaciaContent
case class LinkSnap(
  id: String,
  snapUri: Option[String],
  snapCss: Option[String]) extends Snap

object LatestSnap {
  //def contentFromSnapUri(uri: String): Content =

}

case class LatestSnap(
  id: String,
  snapUri: Option[String],
  snapCss: Option[String],
  latestContent: Option[Content]) extends Snap

case class CuratedContent(
  content: Content,
  headline: String,
  href: Option[String],
  trailText: Option[String],
  group: String,
  image: Option[Image],
  isBreaking: Boolean,
  isBoosted: Boolean,
  imageHide: Boolean,
  imageReplace: Boolean,
  showMainVideo: Boolean,
  showKickerTag: Boolean,
  byline: Option[String],
  showByLine: Boolean,
  kicker: Option[ItemKicker],
  imageCutout: Option[ImageCutout],
  showBoostedHeadline: Boolean,
  showQuotedHeadline: Boolean) extends FaciaContent

object FaciaContent {

  def fromTrailAndContent(content: Content, trailMetaData: TrailMetaData, collectionConfig: CollectionConfig): CuratedContent = {
    val contentFields: Map[String, String] = content.safeFields

    CuratedContent(
      content,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href.orElse(contentFields.get("href")),
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      Image.fromTrail(trailMetaData),
      trailMetaData.isBreaking.getOrElse(false),
      trailMetaData.isBoosted.getOrElse(false),
      trailMetaData.imageHide.getOrElse(false),
      trailMetaData.imageReplace.getOrElse(false),
      trailMetaData.showMainVideo.getOrElse(false),
      trailMetaData.showKickerTag.getOrElse(false),
      trailMetaData.byline.orElse(contentFields.get("byline")),
      trailMetaData.showByline.getOrElse(false),
      ItemKicker.fromContentAndTrail(content, trailMetaData, Some(collectionConfig)),
      ImageCutout.fromTrail(trailMetaData),
      trailMetaData.showBoostedHeadline.getOrElse(false),
      trailMetaData.showQuotedHeadline.getOrElse(false)
    )
  }
}