package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.ItemKicker
import com.gu.facia.client.models.{SupportingItem, MetaDataCommonFields, Trail, TrailMetaData}

case class Image(imageSrc: String, imageSrcWidth: String, imageSrcHeight: String)

object Image {
  def fromTrail(trailMeta: MetaDataCommonFields): Option[Image] =
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
  def fromTrail(trailMeta: MetaDataCommonFields): Option[ImageCutout] =
    for {
      imageCutoutSrc <- trailMeta.imageCutoutSrc
      imageCutoutSrcWidth <- trailMeta.imageCutoutSrcWidth
      imageCutoutSrcHeight <- trailMeta.imageCutoutSrcHeight
    } yield ImageCutout(trailMeta.imageCutoutReplace.getOrElse(false),
      imageCutoutSrc, imageCutoutSrcWidth, imageCutoutSrcHeight)
}

sealed trait FaciaContent

object Snap {
  val LatestType = "latest"
  val LinkType = "link"
  val DefaultType = LinkType

  def maybeFromTrail(trail: Trail): Option[Snap] = trail.safeMeta.snapType match {
    case Some("latest") =>
      Option(LatestSnap.fromTrailAndContent(trail, None))
    case Some(snapType) =>
      Option(LinkSnap(
      trail.id,
      snapType,
      trail.safeMeta.snapUri,
      trail.safeMeta.snapCss,
      trail.safeMeta.headline,
      trail.safeMeta.href,
      trail.safeMeta.trailText,
      trail.safeMeta.group.getOrElse("0"),
      Image.fromTrail(trail.safeMeta),
      trail.safeMeta.isBreaking.exists(identity),
      trail.safeMeta.isBoosted.exists(identity),
      trail.safeMeta.imageHide.exists(identity),
      trail.safeMeta.imageReplace.exists(identity),
      trail.safeMeta.showMainVideo.exists(identity),
      trail.safeMeta.showKickerTag.exists(identity),
      trail.safeMeta.byline,
      trail.safeMeta.showByline.exists(identity),
      ItemKicker.fromTrailMetaData(trail.safeMeta),
      ImageCutout.fromTrail(trail.safeMeta),
      trail.safeMeta.showBoostedHeadline.exists(identity),
      trail.safeMeta.showQuotedHeadline.exists(identity)))
    case _ => None
  }

  def maybeFromSupportingItem(supportingItem: SupportingItem): Option[Snap] = supportingItem.safeMeta.snapType match {
    case Some("latest") =>
      Option(LatestSnap.fromSupportingItemAndContent(supportingItem, None))
    case Some(snapType) =>
      Option(LinkSnap(
      supportingItem.id,
      snapType,
      supportingItem.safeMeta.snapUri,
      supportingItem.safeMeta.snapCss,
      supportingItem.safeMeta.headline,
      supportingItem.safeMeta.href,
      supportingItem.safeMeta.trailText,
      supportingItem.safeMeta.group.getOrElse("0"),
      Image.fromTrail(supportingItem.safeMeta),
      supportingItem.safeMeta.isBreaking.exists(identity),
      supportingItem.safeMeta.isBoosted.exists(identity),
      supportingItem.safeMeta.imageHide.exists(identity),
      supportingItem.safeMeta.imageReplace.exists(identity),
      supportingItem.safeMeta.showMainVideo.exists(identity),
      supportingItem.safeMeta.showKickerTag.exists(identity),
      supportingItem.safeMeta.byline,
      supportingItem.safeMeta.showByline.exists(identity),
      ItemKicker.fromTrailMetaData(supportingItem.safeMeta),
      ImageCutout.fromTrail(supportingItem.safeMeta),
      supportingItem.safeMeta.showBoostedHeadline.exists(identity),
      supportingItem.safeMeta.showQuotedHeadline.exists(identity)
  ))
    case _ => None
  }
}

sealed trait Snap extends FaciaContent
case class LinkSnap(
  id: String,
  snapType: String,
  snapUri: Option[String],
  snapCss: Option[String],
  headline: Option[String],
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
  showQuotedHeadline: Boolean) extends Snap

case class LatestSnap(
  id: String,
  snapUri: Option[String],
  snapCss: Option[String],
  latestContent: Option[Content],
  headline: Option[String],
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
  showQuotedHeadline: Boolean) extends Snap

object LatestSnap {
  def fromTrailAndContent(trail: Trail, maybeContent: Option[Content]): LatestSnap =
    LatestSnap(
      trail.id,
      trail.safeMeta.snapUri,
      trail.safeMeta.snapCss,
      maybeContent,
      trail.safeMeta.headline,
      trail.safeMeta.href,
      trail.safeMeta.trailText,
      trail.safeMeta.group.getOrElse("0"),
      Image.fromTrail(trail.safeMeta),
      trail.safeMeta.isBreaking.exists(identity),
      trail.safeMeta.isBoosted.exists(identity),
      trail.safeMeta.imageHide.exists(identity),
      trail.safeMeta.imageReplace.exists(identity),
      trail.safeMeta.showMainVideo.exists(identity),
      trail.safeMeta.showKickerTag.exists(identity),
      trail.safeMeta.byline,
      trail.safeMeta.showByline.exists(identity),
      ItemKicker.fromTrailMetaData(trail.safeMeta),
      ImageCutout.fromTrail(trail.safeMeta),
      trail.safeMeta.showBoostedHeadline.exists(identity),
      trail.safeMeta.showQuotedHeadline.exists(identity)
    )

  def fromSupportingItemAndContent(supportingItem: SupportingItem, maybeContent: Option[Content]): LatestSnap =
    LatestSnap(
      supportingItem.id,
      supportingItem.safeMeta.snapUri,
      supportingItem.safeMeta.snapCss,
      maybeContent,
      supportingItem.safeMeta.headline,
      supportingItem.safeMeta.href,
      supportingItem.safeMeta.trailText,
      supportingItem.safeMeta.group.getOrElse("0"),
      Image.fromTrail(supportingItem.safeMeta),
      supportingItem.safeMeta.isBreaking.exists(identity),
      supportingItem.safeMeta.isBoosted.exists(identity),
      supportingItem.safeMeta.imageHide.exists(identity),
      supportingItem.safeMeta.imageReplace.exists(identity),
      supportingItem.safeMeta.showMainVideo.exists(identity),
      supportingItem.safeMeta.showKickerTag.exists(identity),
      supportingItem.safeMeta.byline,
      supportingItem.safeMeta.showByline.exists(identity),
      ItemKicker.fromTrailMetaData(supportingItem.safeMeta),
      ImageCutout.fromTrail(supportingItem.safeMeta),
      supportingItem.safeMeta.showBoostedHeadline.exists(identity),
      supportingItem.safeMeta.showQuotedHeadline.exists(identity)
    )
}

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
      trailMetaData.showQuotedHeadline.getOrElse(false))}
}