package com.gu.facia.api.models

import com.gu.contentapi.client.model.{Tag, Content}
import com.gu.facia.api.utils._
import com.gu.facia.client.models.{SupportingItem, MetaDataCommonFields, Trail, TrailMetaData}

case class ImageReplace(imageSrc: String, imageSrcWidth: String, imageSrcHeight: String)

object ImageReplace {
  def fromTrailMeta(trailMeta: MetaDataCommonFields): Option[ImageReplace] =
    for {
      imageReplace <- trailMeta.imageReplace.filter(identity)
      imageSrc <- trailMeta.imageSrc
      imageSrcWidth <- trailMeta.imageSrcWidth
      imageSrcHeight <- trailMeta.imageSrcHeight
    } yield ImageReplace(imageSrc, imageSrcWidth, imageSrcHeight)
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
    if (contributorTags.length == 1)
      for {
        tag <- contributorTags.find(_.bylineLargeImageUrl.isDefined)
        path <- tag.bylineLargeImageUrl
      } yield ImageCutout(
        path,
        None,
        None)
    else
      None
  }

  def fromContentAndTrailMeta(content: Content, trailMeta: MetaDataCommonFields, resolvedMetaData: ResolvedMetaData): Option[ImageCutout] = {
    if (resolvedMetaData.imageCutoutReplace)
      fromTrailMeta(trailMeta)
        .orElse(fromContentTags(content, trailMeta))
    else
      None
  }
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
      ImageReplace.fromTrailMeta(trail.safeMeta),
      trail.safeMeta.isBreaking.exists(identity),
      trail.safeMeta.isBoosted.exists(identity),
      trail.safeMeta.imageHide.exists(identity),
      trail.safeMeta.imageReplace.exists(identity),
      trail.safeMeta.showMainVideo.exists(identity),
      trail.safeMeta.showKickerTag.exists(identity),
      trail.safeMeta.byline,
      trail.safeMeta.showByline.exists(identity),
      ItemKicker.fromTrailMetaData(trail.safeMeta),
      ImageCutout.fromTrailMeta(trail.safeMeta),
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
      ImageReplace.fromTrailMeta(supportingItem.safeMeta),
      supportingItem.safeMeta.isBreaking.exists(identity),
      supportingItem.safeMeta.isBoosted.exists(identity),
      supportingItem.safeMeta.imageHide.exists(identity),
      supportingItem.safeMeta.imageReplace.exists(identity),
      supportingItem.safeMeta.showMainVideo.exists(identity),
      supportingItem.safeMeta.showKickerTag.exists(identity),
      supportingItem.safeMeta.byline,
      supportingItem.safeMeta.showByline.exists(identity),
      ItemKicker.fromTrailMetaData(supportingItem.safeMeta),
      ImageCutout.fromTrailMeta(supportingItem.safeMeta),
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
  image: Option[ImageReplace],
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
  image: Option[ImageReplace],
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
  def fromTrailAndContent(trail: Trail, maybeContent: Option[Content]): LatestSnap = {
    val cardStyle: CardStyle = maybeContent.map(CardStyle.apply(_, trail.safeMeta)).getOrElse(Default)
    val resolvedMetaData: ResolvedMetaData =
      maybeContent.fold(ResolvedMetaData.fromTrailMetaData(trail.safeMeta))(ResolvedMetaData.fromContentAndTrailMetaData(_, trail.safeMeta, cardStyle))
    LatestSnap(
      trail.id,
      trail.safeMeta.snapUri,
      trail.safeMeta.snapCss,
      maybeContent,
      trail.safeMeta.headline,
      trail.safeMeta.href,
      trail.safeMeta.trailText,
      trail.safeMeta.group.getOrElse("0"),
      ImageReplace.fromTrailMeta(trail.safeMeta),
      resolvedMetaData.isBreaking,
      resolvedMetaData.isBoosted,
      resolvedMetaData.imageHide,
      resolvedMetaData.imageReplace,
      resolvedMetaData.showMainVideo,
      resolvedMetaData.showKickerTag,
      trail.safeMeta.byline,
      resolvedMetaData.showByline,
      ItemKicker.fromMaybeContentTrailMetaAndResolvedMetaData(maybeContent, trail.safeMeta, resolvedMetaData),
      maybeContent.fold(ImageCutout.fromTrailMeta(trail.safeMeta))(ImageCutout.fromContentAndTrailMeta(_, trail.safeMeta, resolvedMetaData)),
      resolvedMetaData.showBoostedHeadline,
      resolvedMetaData.showQuotedHeadline
    )
  }

  def fromSupportingItemAndContent(supportingItem: SupportingItem, maybeContent: Option[Content]): LatestSnap = {
    val cardStyle: CardStyle = maybeContent.map(CardStyle.apply(_, supportingItem.safeMeta)).getOrElse(Default)
    val resolvedMetaData: ResolvedMetaData =
      maybeContent.fold(ResolvedMetaData.fromTrailMetaData(supportingItem.safeMeta))(ResolvedMetaData.fromContentAndTrailMetaData(_, supportingItem.safeMeta, cardStyle))
    LatestSnap(
      supportingItem.id,
      supportingItem.safeMeta.snapUri,
      supportingItem.safeMeta.snapCss,
      maybeContent,
      supportingItem.safeMeta.headline,
      supportingItem.safeMeta.href,
      supportingItem.safeMeta.trailText,
      supportingItem.safeMeta.group.getOrElse("0"),
      ImageReplace.fromTrailMeta(supportingItem.safeMeta),
      resolvedMetaData.isBreaking,
      resolvedMetaData.isBoosted,
      resolvedMetaData.imageHide,
      resolvedMetaData.imageReplace,
      resolvedMetaData.showMainVideo,
      resolvedMetaData.showKickerTag,
      supportingItem.safeMeta.byline,
      resolvedMetaData.showByline,
      ItemKicker.fromMaybeContentTrailMetaAndResolvedMetaData(maybeContent, supportingItem.safeMeta, resolvedMetaData),
      maybeContent.fold(ImageCutout.fromTrailMeta(supportingItem.safeMeta))(ImageCutout.fromContentAndTrailMeta(_, supportingItem.safeMeta, resolvedMetaData)),
      resolvedMetaData.showBoostedHeadline,
      resolvedMetaData.showQuotedHeadline
    )
  }
}

case class CuratedContent(
  content: Content,
  supportingContent: List[FaciaContent],
  cardStyle: CardStyle,
  headline: String,
  href: Option[String],
  trailText: Option[String],
  group: String,
  imageReplace: Option[ImageReplace],
  properties: ContentProperties,
  byline: Option[String],
  kicker: Option[ItemKicker],
  imageCutout: Option[ImageCutout],
  embedType: Option[String],
  embedUri: Option[String],
  embedCss: Option[String]) extends FaciaContent

case class SupportingCuratedContent(
  content: Content,
  headline: String,
  href: Option[String],
  trailText: Option[String],
  group: String,
  imageReplace: Option[ImageReplace],
  isBreaking: Boolean,
  isBoosted: Boolean,
  imageHide: Boolean,
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
    val cardStyle = CardStyle(content, trailMetaData)
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(content, trailMetaData, cardStyle)

    CuratedContent(
      content,
      supportingContent,
      cardStyle,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href.orElse(contentFields.get("href")),
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      ImageReplace.fromTrailMeta(trailMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      trailMetaData.byline.orElse(contentFields.get("byline")),
      ItemKicker.fromContentAndTrail(Option(content), trailMetaData, resolvedMetaData, Some(collectionConfig)),
      ImageCutout.fromContentAndTrailMeta(content, trailMetaData, resolvedMetaData),
      embedType = trailMetaData.snapType,
      embedUri = trailMetaData.snapUri,
      embedCss = trailMetaData.snapCss)}

  def fromTrailAndContent(content: Content, trailMetaData: MetaDataCommonFields, collectionConfig: CollectionConfig): CuratedContent = {
    val contentFields: Map[String, String] = content.safeFields
    val cardStyle = CardStyle(content, trailMetaData)
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(content, trailMetaData, cardStyle)

    CuratedContent(
      content,
      supportingContent = Nil,
      cardStyle = cardStyle,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href.orElse(contentFields.get("href")),
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      ImageReplace.fromTrailMeta(trailMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      trailMetaData.byline.orElse(contentFields.get("byline")),
      ItemKicker.fromContentAndTrail(Option(content), trailMetaData, resolvedMetaData, Some(collectionConfig)),
      ImageCutout.fromContentAndTrailMeta(content, trailMetaData, resolvedMetaData),
      embedType = trailMetaData.snapType,
      embedUri = trailMetaData.snapUri,
      embedCss = trailMetaData.snapCss)}
}

object SupportingCuratedContent {
  def fromTrailAndContent(content: Content, trailMetaData: MetaDataCommonFields, collectionConfig: CollectionConfig): SupportingCuratedContent = {
    val contentFields: Map[String, String] = content.safeFields
    val cardStyle = CardStyle(content, trailMetaData)
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(content, trailMetaData, cardStyle)

    SupportingCuratedContent(
      content,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href.orElse(contentFields.get("href")),
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      ImageReplace.fromTrailMeta(trailMetaData),
      resolvedMetaData.isBreaking,
      resolvedMetaData.isBoosted,
      resolvedMetaData.imageHide,
      resolvedMetaData.showMainVideo,
      resolvedMetaData.showKickerTag,
      trailMetaData.byline.orElse(contentFields.get("byline")),
      resolvedMetaData.showByline,
      ItemKicker.fromContentAndTrail(Option(content), trailMetaData, resolvedMetaData, None),
      ImageCutout.fromContentAndTrailMeta(content, trailMetaData, resolvedMetaData),
      resolvedMetaData.showBoostedHeadline,
      resolvedMetaData.showQuotedHeadline)}
}