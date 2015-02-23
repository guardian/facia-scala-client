package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.ItemKicker
import com.gu.facia.client.models.{SupportingItem, MetaDataCommonFields, Trail, TrailMetaData}

case class Image(imageSrc: String, imageSrcWidth: String, imageSrcHeight: String)

object Image {
  def fromTrailMeta(trailMeta: MetaDataCommonFields): Option[Image] =
    for {
      imageSrc <- trailMeta.imageSrc
      imageSrcWidth <- trailMeta.imageSrcWidth
      imageSrcHeight <- trailMeta.imageSrcHeight
    } yield Image(imageSrc, imageSrcWidth, imageSrcHeight)
}

case class ImageCutout(
  imageCutoutSrc: String,
  imageCutoutSrcWidth: Option[String],
  imageCutoutSrcHeight: Option[String])

object ImageCutout {
  def fromTrailMeta(trailMeta: MetaDataCommonFields): Option[ImageCutout] =
    for {
      src <- trailMeta.imageCutoutSrc
      width <- trailMeta.imageCutoutSrcWidth
      height <- trailMeta.imageCutoutSrcHeight
    } yield ImageCutout(
              src,
              Option(width),
              Option(height))

  def fromContentTags(content: Content, trailMeta: MetaDataCommonFields): Option[ImageCutout] = {
    val contributorTags = content.tags.filter(_.`type` == "contributor")
      for {
        tag <- contributorTags.find(_.bylineLargeImageUrl.isDefined)
        path <- tag.bylineLargeImageUrl
      } yield ImageCutout(
        path,
        None,
        None)
  }

  def fromContentAndTrailMeta(content: Content, trailMeta: MetaDataCommonFields): Option[ImageCutout] =
    if (trailMeta.imageCutoutReplace.exists(identity))
      fromTrailMeta(trailMeta)
        .orElse(fromContentTags(content, trailMeta))
    else
      None
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

  def maybeFromSupportingItem(supportingItem: SupportingItem): Option[Snap] = supportingItem.safeMeta.snapType match {
    case Some("link") =>
      Option(LinkSnap(
        supportingItem.id,
        supportingItem.safeMeta.snapUri,
        supportingItem.safeMeta.snapCss))
    case Some("latest") =>
      Option(LatestSnap(
        supportingItem.id,
        supportingItem.safeMeta.snapUri,
        supportingItem.safeMeta.snapCss,
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

case class LatestSnap(
  id: String,
  snapUri: Option[String],
  snapCss: Option[String],
  latestContent: Option[Content]) extends Snap

case class CuratedContent(
  content: Content,
  supportingContent: List[FaciaContent],
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

case class SupportingCuratedContent(
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

object CuratedContent {

  def fromTrailAndContentWithSupporting(content: Content, trailMetaData: TrailMetaData,
                                        supportingContent: List[FaciaContent],
                                        collectionConfig: CollectionConfig) = {
    val contentFields: Map[String, String] = content.safeFields

    CuratedContent(
      content,
      supportingContent,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href.orElse(contentFields.get("href")),
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      Image.fromTrailMeta(trailMetaData),
      trailMetaData.isBreaking.getOrElse(false),
      trailMetaData.isBoosted.getOrElse(false),
      trailMetaData.imageHide.getOrElse(false),
      trailMetaData.imageReplace.getOrElse(false),
      trailMetaData.showMainVideo.getOrElse(false),
      trailMetaData.showKickerTag.getOrElse(false),
      trailMetaData.byline.orElse(contentFields.get("byline")),
      trailMetaData.showByline.getOrElse(false),
      ItemKicker.fromContentAndTrail(content, trailMetaData, Some(collectionConfig)),
      ImageCutout.fromContentAndTrailMeta(content, trailMetaData),
      trailMetaData.showBoostedHeadline.getOrElse(false),
      trailMetaData.showQuotedHeadline.getOrElse(false))}

  def fromTrailAndContent(content: Content, trailMetaData: MetaDataCommonFields, collectionConfig: CollectionConfig): CuratedContent = {
    val contentFields: Map[String, String] = content.safeFields

    CuratedContent(
      content,
      supportingContent = Nil,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href.orElse(contentFields.get("href")),
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      Image.fromTrailMeta(trailMetaData),
      trailMetaData.isBreaking.getOrElse(false),
      trailMetaData.isBoosted.getOrElse(false),
      trailMetaData.imageHide.getOrElse(false),
      trailMetaData.imageReplace.getOrElse(false),
      trailMetaData.showMainVideo.getOrElse(false),
      trailMetaData.showKickerTag.getOrElse(false),
      trailMetaData.byline.orElse(contentFields.get("byline")),
      trailMetaData.showByline.getOrElse(false),
      ItemKicker.fromContentAndTrail(content, trailMetaData, Some(collectionConfig)),
      ImageCutout.fromContentAndTrailMeta(content, trailMetaData),
      trailMetaData.showBoostedHeadline.getOrElse(false),
      trailMetaData.showQuotedHeadline.getOrElse(false)
    )}
}

object SupportingCuratedContent {
  def fromTrailAndContent(content: Content, trailMetaData: MetaDataCommonFields, collectionConfig: CollectionConfig): SupportingCuratedContent = {
    val contentFields: Map[String, String] = content.safeFields

    SupportingCuratedContent(
      content,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href.orElse(contentFields.get("href")),
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      Image.fromTrailMeta(trailMetaData),
      trailMetaData.isBreaking.getOrElse(false),
      trailMetaData.isBoosted.getOrElse(false),
      trailMetaData.imageHide.getOrElse(false),
      trailMetaData.imageReplace.getOrElse(false),
      trailMetaData.showMainVideo.getOrElse(false),
      trailMetaData.showKickerTag.getOrElse(false),
      trailMetaData.byline.orElse(contentFields.get("byline")),
      trailMetaData.showByline.getOrElse(false),
      ItemKicker.fromContentAndTrail(content, trailMetaData, Some(collectionConfig)),
      ImageCutout.fromContentAndTrailMeta(content, trailMetaData),
      trailMetaData.showBoostedHeadline.getOrElse(false),
      trailMetaData.showQuotedHeadline.getOrElse(false))}
}