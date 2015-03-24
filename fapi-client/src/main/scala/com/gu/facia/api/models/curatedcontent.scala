package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.{ResolvedMetaData, ItemKicker}
import com.gu.facia.client.models.{SupportingItem, MetaDataCommonFields, Trail, TrailMetaData}

case class Image(
  imageType: String,
  imageSrc: String,
  imageSrcWidth: Option[String],
  imageSrcHeight: Option[String]
)

object Image {
  def fromTrailMeta(trailMeta: MetaDataCommonFields): Option[Image] = {

    val maybeCutout = {
      for {
        imageCutout <- trailMeta.imageCutoutReplace.filter(identity)
        src <- trailMeta.imageCutoutSrc
        width <- trailMeta.imageCutoutSrcWidth
        height <- trailMeta.imageCutoutSrcHeight
      } yield Image("cutout", src, Option(width), Option(height))
    }

    val maybeReplace = {
      for {
        imageReplace <- trailMeta.imageReplace.filter(identity)
        src <- trailMeta.imageSrc
        width <- trailMeta.imageSrcWidth
        height <- trailMeta.imageSrcHeight
      } yield Image("replace", src, Option(width), Option(height))
    }

    val defaultImage = {
      for {
        src <- trailMeta.imageSrc
        width <- trailMeta.imageSrcWidth
        height <- trailMeta.imageSrcHeight
      } yield Image("default", src, Option(width), Option(height))
    }

    if (trailMeta.imageHide.exists(identity)) None else maybeCutout orElse maybeReplace orElse defaultImage

  }

  def fromContentTags(content: Content, trailMeta: MetaDataCommonFields): Option[Image] = {
    val contributorTags = content.tags.filter(_.`type` == "contributor")
    for {
      tag <- contributorTags.find(_.bylineLargeImageUrl.isDefined)
      path <- tag.bylineLargeImageUrl
    } yield Image("cutout", path, None, None)
  }

  def fromContentAndTrailMeta(content: Content, trailMeta: MetaDataCommonFields): Option[Image] = {
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(content, trailMeta)
    if (resolvedMetaData.imageCutoutReplace)
      fromTrailMeta(trailMeta)
        .orElse(fromContentTags(content, trailMeta))
    else
      None
  }
}

sealed trait FaciaContent

object FaciaContent {

  def fold[T](fc: FaciaContent)(c: (CuratedContent) => T, scc: (SupportingCuratedContent) => T,
              ls: (LinkSnap) => T, las: (LatestSnap) => T): T = fc match {
    case curatedContent: CuratedContent => c(curatedContent)
    case supportingCuratedContent: SupportingCuratedContent => scc(supportingCuratedContent)
    case linkSnap: LinkSnap => ls(linkSnap)
    case latestSnap: LatestSnap => las(latestSnap)
  }

  def id(fc: FaciaContent): String = fold(fc)(
    curatedContent => curatedContent.content.id,
    supportingCuratedContent => supportingCuratedContent.content.id,
    linkSnap => linkSnap.id,
    latestSnap => latestSnap.id)

  def content(fc: FaciaContent): Option[Content] = fold(fc)(
    curatedContent => Option(curatedContent.content),
    supportingCuratedContent => Option(supportingCuratedContent.content),
    linkSnap => None,
    latestSnap => latestSnap.latestContent)

  def supportingContent(fc: FaciaContent): List[FaciaContent] = fold(fc)(
    curatedContent => curatedContent.supportingContent,
    supportingCuratedContent => Nil,
    linkSnap => Nil,
    latestSnap => Nil)

  def headline(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => Some(curatedContent.headline),
    supportingCuratedContent => Some(supportingCuratedContent.headline),
    linkSnap => linkSnap.headline,
    latestSnap => latestSnap.headline.orElse(latestSnap.latestContent.map(_.webTitle)))

  def href(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.href,
    supportingCuratedContent => supportingCuratedContent.href,
    linkSnap => linkSnap.snapUri,
    latestSnap => latestSnap.snapUri
  )

  def trailText(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.trailText,
    supportingCuratedContent => supportingCuratedContent.trailText,
    linkSnap => None,
    latestSnap => None)

  def group(fc: FaciaContent): String = fold(fc)(
    curatedContent => curatedContent.group,
    supportingCuratedContent => supportingCuratedContent.group,
    linkSnap => linkSnap.group,
    latestSnap => latestSnap.group
  )

  def snapType(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => None,
    supportingCuratedContent => None,
    linkSnap => Option("LinkSnap"),
    latestSnap => Option("LatestSnap"))

  def image(fc: FaciaContent): Option[Image] = fold(fc)(
    curatedContent => curatedContent.image,
    supportingCuratedContent => supportingCuratedContent.image,
    linkSnap => None,
    latestSnap => latestSnap.image
  )

  def isBreaking(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.isBreaking,
    supportingCuratedContent => supportingCuratedContent.isBreaking,
    linkSnap => false,
    latestSnap => false
  )

  def isBoosted(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.isBoosted,
    supportingCuratedContent => supportingCuratedContent.isBoosted,
    linkSnap => false,
    latestSnap => false
  )

  def showMainVideo(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.showMainVideo,
    supportingCuratedContent => supportingCuratedContent.showMainVideo,
    linkSnap => false,
    latestSnap => latestSnap.showMainVideo
  )

  def showKickerTag(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.showKickerTag,
    supportingCuratedContent => supportingCuratedContent.showKickerTag,
    linkSnap => linkSnap.showKickerTag,
    latestSnap => latestSnap.showKickerTag
  )

  def byline(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.byline,
    supportingCuratedContent => supportingCuratedContent.byline,
    linkSnap => None,
    latestSnap => latestSnap.latestContent.flatMap(_.safeFields.get("byline"))
  )

  def showByline(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.showByLine,
    supportingCuratedContent => supportingCuratedContent.showByLine,
    linkSnap => false,
    latestSnap => false
  )

  def kicker(fc: FaciaContent): Option[ItemKicker] =
    fold(fc)(
      curatedContent => curatedContent.kicker,
      supportingCuratedContent => supportingCuratedContent.kicker,
      linkSnap => linkSnap.kicker,
      latestSnap => latestSnap.kicker)

  def showBoostedHeadline(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.showBoostedHeadline,
    supportingCuratedContent => supportingCuratedContent.showBoostedHeadline,
    linkSnap => false,
    latestSnap => false
  )

  def showQuotedHeadline(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.showQuotedHeadline,
    supportingCuratedContent => supportingCuratedContent.showQuotedHeadline,
    linkSnap => false,
    latestSnap => false
  )
}

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
      Image.fromTrailMeta(trail.safeMeta),
      trail.safeMeta.isBreaking.exists(identity),
      trail.safeMeta.isBoosted.exists(identity),
      trail.safeMeta.showMainVideo.exists(identity),
      trail.safeMeta.showKickerTag.exists(identity),
      trail.safeMeta.byline,
      trail.safeMeta.showByline.exists(identity),
      ItemKicker.fromTrailMetaData(trail.safeMeta),
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
      Image.fromTrailMeta(supportingItem.safeMeta),
      supportingItem.safeMeta.isBreaking.exists(identity),
      supportingItem.safeMeta.isBoosted.exists(identity),
      supportingItem.safeMeta.showMainVideo.exists(identity),
      supportingItem.safeMeta.showKickerTag.exists(identity),
      supportingItem.safeMeta.byline,
      supportingItem.safeMeta.showByline.exists(identity),
      ItemKicker.fromTrailMetaData(supportingItem.safeMeta),
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
  showMainVideo: Boolean,
  showKickerTag: Boolean,
  byline: Option[String],
  showByLine: Boolean,
  kicker: Option[ItemKicker],
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
  showMainVideo: Boolean,
  showKickerTag: Boolean,
  byline: Option[String],
  showByLine: Boolean,
  kicker: Option[ItemKicker],
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
      Image.fromTrailMeta(trail.safeMeta),
      trail.safeMeta.isBreaking.exists(identity),
      trail.safeMeta.isBoosted.exists(identity),
      trail.safeMeta.showMainVideo.exists(identity),
      trail.safeMeta.showKickerTag.exists(identity),
      trail.safeMeta.byline,
      trail.safeMeta.showByline.exists(identity),
      ItemKicker.fromTrailMetaData(trail.safeMeta),
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
      Image.fromTrailMeta(supportingItem.safeMeta),
      supportingItem.safeMeta.isBreaking.exists(identity),
      supportingItem.safeMeta.isBoosted.exists(identity),
      supportingItem.safeMeta.showMainVideo.exists(identity),
      supportingItem.safeMeta.showKickerTag.exists(identity),
      supportingItem.safeMeta.byline,
      supportingItem.safeMeta.showByline.exists(identity),
      ItemKicker.fromTrailMetaData(supportingItem.safeMeta),
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
  showMainVideo: Boolean,
  showKickerTag: Boolean,
  byline: Option[String],
  showByLine: Boolean,
  kicker: Option[ItemKicker],
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
  showMainVideo: Boolean,
  showKickerTag: Boolean,
  byline: Option[String],
  showByLine: Boolean,
  kicker: Option[ItemKicker],
  showBoostedHeadline: Boolean,
  showQuotedHeadline: Boolean) extends FaciaContent

object CuratedContent {

  def fromTrailAndContentWithSupporting(content: Content, trailMetaData: TrailMetaData,
                                        supportingContent: List[FaciaContent],
                                        collectionConfig: CollectionConfig) = {
    val contentFields: Map[String, String] = content.safeFields
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(content, trailMetaData)

    CuratedContent(
      content,
      supportingContent,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href.orElse(contentFields.get("href")),
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      Image.fromTrailMeta(trailMetaData),
      resolvedMetaData.isBreaking,
      resolvedMetaData.isBoosted,
      resolvedMetaData.showMainVideo,
      resolvedMetaData.showKickerTag,
      trailMetaData.byline.orElse(contentFields.get("byline")),
      trailMetaData.showByline.getOrElse(false),
      ItemKicker.fromContentAndTrail(content, trailMetaData, resolvedMetaData, Some(collectionConfig)),
      resolvedMetaData.showBoostedHeadline,
      resolvedMetaData.showQuotedHeadline)}

  def fromTrailAndContent(content: Content, trailMetaData: MetaDataCommonFields, collectionConfig: CollectionConfig): CuratedContent = {
    val contentFields: Map[String, String] = content.safeFields
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(content, trailMetaData)

    CuratedContent(
      content,
      supportingContent = Nil,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href.orElse(contentFields.get("href")),
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      Image.fromTrailMeta(trailMetaData),
      resolvedMetaData.isBreaking,
      resolvedMetaData.isBoosted,
      resolvedMetaData.showMainVideo,
      resolvedMetaData.showKickerTag,
      trailMetaData.byline.orElse(contentFields.get("byline")),
      resolvedMetaData.showByline,
      ItemKicker.fromContentAndTrail(content, trailMetaData, resolvedMetaData, Some(collectionConfig)),
      resolvedMetaData.showBoostedHeadline,
      resolvedMetaData.showQuotedHeadline
    )}
}

object SupportingCuratedContent {
  def fromTrailAndContent(content: Content, trailMetaData: MetaDataCommonFields, collectionConfig: CollectionConfig): SupportingCuratedContent = {
    val contentFields: Map[String, String] = content.safeFields
    val resolvedMetaData = ResolvedMetaData.fromContentAndTrailMetaData(content, trailMetaData)

    SupportingCuratedContent(
      content,
      trailMetaData.headline.orElse(content.safeFields.get("headline")).getOrElse(content.webTitle),
      trailMetaData.href.orElse(contentFields.get("href")),
      trailMetaData.trailText.orElse(contentFields.get("trailText")),
      trailMetaData.group.getOrElse("0"),
      Image.fromTrailMeta(trailMetaData),
      resolvedMetaData.isBreaking,
      resolvedMetaData.isBoosted,
      resolvedMetaData.showMainVideo,
      resolvedMetaData.showKickerTag,
      trailMetaData.byline.orElse(contentFields.get("byline")),
      resolvedMetaData.showByline,
      ItemKicker.fromContentAndTrail(content, trailMetaData, resolvedMetaData, None),
      resolvedMetaData.showBoostedHeadline,
      resolvedMetaData.showQuotedHeadline)}
}