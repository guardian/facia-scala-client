package com.gu.facia.api.models

import com.gu.contentapi.client.model.v1.{TagType, Tag, Content}
import com.gu.facia.api.utils._
import com.gu.facia.client.models.{SupportingItem, MetaDataCommonFields, Trail, TrailMetaData}

sealed trait FaciaImage
case class Cutout(imageSrc: String, imageSrcWidth: Option[String], imageSrcHeight: Option[String]) extends FaciaImage
case class Replace(imageSrc: String, imageSrcWidth: String, imageSrcHeight: String) extends FaciaImage
case class ImageSlideshow(assets: List[Replace]) extends FaciaImage

object FaciaImage {

  def getFaciaImage(maybeContent: Option[Content], trailMeta: MetaDataCommonFields, resolvedMetadata: ResolvedMetaData): Option[FaciaImage] = {
    if (resolvedMetadata.imageHide)
      None
    else if (resolvedMetadata.imageSlideshowReplace)
      imageSlideshow(trailMeta, resolvedMetadata)
    else if (resolvedMetadata.imageCutoutReplace)
      imageCutout(trailMeta) orElse maybeContent.flatMap(fromContentTags(_, trailMeta))
    else if (resolvedMetadata.imageReplace)
      imageReplace(trailMeta, resolvedMetadata)
    else None}

  def fromContentTags(content: Content, trailMeta: MetaDataCommonFields): Option[FaciaImage] = {
    val contributorTags = content.tags.filter(_.`type` == "contributor")
    if (contributorTags.length == 1)
      for {
        tag <- contributorTags.find(_.bylineLargeImageUrl.isDefined)
        path <- tag.bylineLargeImageUrl
      } yield Cutout(path, None, None)
    else None
  }

  def imageCutout(trailMeta: MetaDataCommonFields): Option[FaciaImage] = for {
    src <- trailMeta.imageCutoutSrc
    width <- trailMeta.imageCutoutSrcWidth
    height <- trailMeta.imageCutoutSrcHeight
  } yield Cutout(src, Option(width), Option(height))

  def imageReplace(trailMeta: MetaDataCommonFields, resolvedMetaData: ResolvedMetaData): Option[FaciaImage] = for{
        src <- trailMeta.imageSrc
        width <- trailMeta.imageSrcWidth
        height <- trailMeta.imageSrcHeight}
    yield Replace(src, width, height)

  def imageSlideshow(trailMeta: MetaDataCommonFields, resolvedMetaData: ResolvedMetaData): Option[FaciaImage] =
    trailMeta.slideshow.map { assets =>
      val slideshowAssets = assets.map(asset => Replace(asset.src, asset.width, asset.height))
      ImageSlideshow(slideshowAssets)}

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
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(trail.safeMeta)
      val contentProperties = ContentProperties.fromResolvedMetaData(resolvedMetaData)
      Option(LinkSnap(
        trail.id,
        Option(trail.frontPublicationDate),
        snapType,
        trail.safeMeta.snapUri,
        trail.safeMeta.snapCss,
        trail.safeMeta.headline,
        trail.safeMeta.href,
        trail.safeMeta.trailText,
        trail.safeMeta.group.getOrElse("0"),
        FaciaImage.getFaciaImage(None, trail.safeMeta, resolvedMetaData),
        contentProperties,
        trail.safeMeta.byline,
        ItemKicker.fromTrailMetaData(trail.safeMeta)))
    case _ => None
  }

  def maybeFromSupportingItem(supportingItem: SupportingItem): Option[Snap] = supportingItem.safeMeta.snapType match {
    case Some("latest") =>
      Option(LatestSnap.fromSupportingItemAndContent(supportingItem, None))
    case Some(snapType) =>
      val resolvedMetaData = ResolvedMetaData.fromTrailMetaData(supportingItem.safeMeta)
      val contentProperties = ContentProperties.fromResolvedMetaData(resolvedMetaData)
      Option(LinkSnap(
        supportingItem.id,
        supportingItem.frontPublicationDate,
        snapType,
        supportingItem.safeMeta.snapUri,
        supportingItem.safeMeta.snapCss,
        supportingItem.safeMeta.headline,
        supportingItem.safeMeta.href,
        supportingItem.safeMeta.trailText,
        supportingItem.safeMeta.group.getOrElse("0"),
        FaciaImage.getFaciaImage(None, supportingItem.safeMeta, resolvedMetaData),
        contentProperties,
        supportingItem.safeMeta.byline,
        ItemKicker.fromTrailMetaData(supportingItem.safeMeta)))
    case _ => None
  }
}

sealed trait Snap extends FaciaContent
case class LinkSnap(
  id: String,
  maybeFrontPublicationDate: Option[Long],
  snapType: String,
  snapUri: Option[String],
  snapCss: Option[String],
  headline: Option[String],
  href: Option[String],
  trailText: Option[String],
  group: String,
  image: Option[FaciaImage],
  properties: ContentProperties,
  byline: Option[String],
  kicker: Option[ItemKicker]) extends Snap

case class LatestSnap(
  id: String,
  maybeFrontPublicationDate: Option[Long],
  cardStyle: CardStyle,
  snapUri: Option[String],
  snapCss: Option[String],
  latestContent: Option[Content],
  headline: Option[String],
  href: Option[String],
  trailText: Option[String],
  group: String,
  image: Option[FaciaImage],
  properties: ContentProperties,
  byline: Option[String],
  kicker: Option[ItemKicker]) extends Snap

object LatestSnap {
  def fromTrailAndContent(trail: Trail, maybeContent: Option[Content]): LatestSnap = {
    val cardStyle: CardStyle = maybeContent.map(CardStyle.apply(_, trail.safeMeta)).getOrElse(DefaultCardstyle)
    val resolvedMetaData: ResolvedMetaData =
      maybeContent.fold(ResolvedMetaData.fromTrailMetaData(trail.safeMeta))(ResolvedMetaData.fromContentAndTrailMetaData(_, trail.safeMeta, cardStyle))
    LatestSnap(
      trail.id,
      Option(trail.frontPublicationDate),
      cardStyle,
      trail.safeMeta.snapUri,
      trail.safeMeta.snapCss,
      maybeContent,
      trail.safeMeta.headline.orElse(maybeContent.flatMap(_.safeFields.get("headline"))).orElse(maybeContent.map(_.webTitle)),
      trail.safeMeta.href.orElse(maybeContent.map(_.id)),
      trail.safeMeta.trailText.orElse(maybeContent.flatMap(_.safeFields.get("trailText"))),
      trail.safeMeta.group.getOrElse("0"),
      FaciaImage.getFaciaImage(maybeContent, trail.safeMeta, resolvedMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      trail.safeMeta.byline.orElse(maybeContent.flatMap(_.safeFields.get("byline"))),
      ItemKicker.fromMaybeContentTrailMetaAndResolvedMetaData(maybeContent, trail.safeMeta, resolvedMetaData)
    )
  }

  def fromSupportingItemAndContent(supportingItem: SupportingItem, maybeContent: Option[Content]): LatestSnap = {
    val cardStyle: CardStyle = maybeContent.map(CardStyle.apply(_, supportingItem.safeMeta)).getOrElse(DefaultCardstyle)
    val resolvedMetaData: ResolvedMetaData =
      maybeContent.fold(ResolvedMetaData.fromTrailMetaData(supportingItem.safeMeta))(ResolvedMetaData.fromContentAndTrailMetaData(_, supportingItem.safeMeta, cardStyle))
    LatestSnap(
      supportingItem.id,
      supportingItem.frontPublicationDate,
      cardStyle,
      supportingItem.safeMeta.snapUri,
      supportingItem.safeMeta.snapCss,
      maybeContent,
      supportingItem.safeMeta.headline.orElse(maybeContent.flatMap(_.safeFields.get("headline"))).orElse(maybeContent.map(_.webTitle)),
      supportingItem.safeMeta.href.orElse(maybeContent.flatMap(_.safeFields.get("href"))),
      supportingItem.safeMeta.trailText.orElse(maybeContent.flatMap(_.safeFields.get("trailText"))),
      supportingItem.safeMeta.group.getOrElse("0"),
      FaciaImage.getFaciaImage(maybeContent, supportingItem.safeMeta, resolvedMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      supportingItem.safeMeta.byline.orElse(maybeContent.flatMap(_.safeFields.get("byline"))),
      ItemKicker.fromMaybeContentTrailMetaAndResolvedMetaData(maybeContent, supportingItem.safeMeta, resolvedMetaData)
    )
  }
}

case class CuratedContent(
  content: Content,
  maybeFrontPublicationDate: Option[Long],
  supportingContent: List[FaciaContent],
  cardStyle: CardStyle,
  headline: String,
  href: Option[String],
  trailText: Option[String],
  group: String,
  image: Option[FaciaImage],
  properties: ContentProperties,
  byline: Option[String],
  kicker: Option[ItemKicker],
  embedType: Option[String],
  embedUri: Option[String],
  embedCss: Option[String]) extends FaciaContent

case class SupportingCuratedContent(
  content: Content,
  maybeFrontPublicationDate: Option[Long],
  cardStyle: CardStyle,
  headline: String,
  href: Option[String],
  trailText: Option[String],
  group: String,
  image: Option[FaciaImage],
  properties: ContentProperties,
  byline: Option[String],
  kicker: Option[ItemKicker]) extends FaciaContent

object CuratedContent {

  def fromTrailAndContentWithSupporting(content: Content,
                                        trailMetaData: TrailMetaData,
                                        maybeFrontPublicationDate: Option[Long],
                                        supportingContent: List[FaciaContent],
                                        collectionConfig: CollectionConfig) = {
    val contentFields: Map[String, String] = content.safeFields
    val cardStyle = CardStyle(content, trailMetaData)
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(content, trailMetaData, cardStyle)

    CuratedContent(
      content,
      maybeFrontPublicationDate,
      supportingContent,
      cardStyle,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href,
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      FaciaImage.getFaciaImage(Some(content), trailMetaData, resolvedMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      trailMetaData.byline.orElse(contentFields.get("byline")),
      ItemKicker.fromContentAndTrail(Option(content), trailMetaData, resolvedMetaData, Some(collectionConfig)),
      embedType = trailMetaData.snapType,
      embedUri = trailMetaData.snapUri,
      embedCss = trailMetaData.snapCss)}

  def fromTrailAndContent(content: Content,
                          trailMetaData: MetaDataCommonFields,
                          maybeFrontPublicationDate: Option[Long],
                          collectionConfig: CollectionConfig): CuratedContent = {

    val contentFields: Map[String, String] = content.safeFields
    val cardStyle = CardStyle(content, trailMetaData)
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(content, trailMetaData, cardStyle)

    CuratedContent(
      content,
      maybeFrontPublicationDate,
      supportingContent = Nil,
      cardStyle = cardStyle,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href,
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      FaciaImage.getFaciaImage(Some(content), trailMetaData, resolvedMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      trailMetaData.byline.orElse(contentFields.get("byline")),
      ItemKicker.fromContentAndTrail(Option(content), trailMetaData, resolvedMetaData, Some(collectionConfig)),
      embedType = trailMetaData.snapType,
      embedUri = trailMetaData.snapUri,
      embedCss = trailMetaData.snapCss)}
}

object SupportingCuratedContent {
  def fromTrailAndContent(content: Content,
                          trailMetaData: MetaDataCommonFields,
                          maybeFrontPublicationDate: Option[Long],
                          collectionConfig: CollectionConfig): SupportingCuratedContent = {
    val contentFields: Map[String, String] = content.safeFields
    val cardStyle = CardStyle(content, trailMetaData)
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(content, trailMetaData, cardStyle)

    SupportingCuratedContent(
      content,
      maybeFrontPublicationDate,
      cardStyle,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href,
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      FaciaImage.getFaciaImage(Some(content), trailMetaData, resolvedMetaData),
      ContentProperties.fromResolvedMetaData(resolvedMetaData),
      trailMetaData.byline.orElse(contentFields.get("byline")),
      ItemKicker.fromContentAndTrail(Option(content), trailMetaData, resolvedMetaData, None)
    )
  }
}