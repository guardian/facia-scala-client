package com.gu.facia.api.utils

import com.gu.contentapi.client.model.Content
import com.gu.facia.client.models.MetaDataCommonFields


object ResolvedMetaData {
  val Cartoon = "type/cartoon"
  val Video = "type/video"
  val Comment = "tone/comment"

  def isCartoonForContent(content: Content): Boolean =
    content.tags.exists(_.id == Cartoon)

  def isCommentForContent(content: Content): Boolean =
    content.tags.exists(_.id == Comment)

  def isVideoForContent(content: Content): Boolean =
    content.tags.exists(_.id == Video)

  val Default = ResolvedMetaData(
    isBreaking = false,
    isBoosted = false,
    imageHide = false,
    imageReplace = false,
    showKickerSection = false,
    showKickerCustom = false,
    showBoostedHeadline = false,
    showMainVideo = false,
    showKickerTag = false,
    showByline = false,
    imageCutoutReplace = false,
    showQuotedHeadline = false,
    imageSlideshowReplace = false)

  def fromTrailMetaData(trailMeta: MetaDataCommonFields): ResolvedMetaData =
    ResolvedMetaData(
      isBreaking = trailMeta.isBreaking.exists(identity),
      isBoosted = trailMeta.isBoosted.exists(identity),
      imageHide = trailMeta.imageHide.exists(identity),
      imageReplace = trailMeta.imageReplace.exists(identity),
      showKickerSection = trailMeta.showKickerSection.exists(identity),
      showKickerCustom = trailMeta.showKickerCustom.exists(identity),
      showBoostedHeadline = trailMeta.showBoostedHeadline.exists(identity),
      showMainVideo = trailMeta.showMainVideo.exists(identity),
      showKickerTag = trailMeta.showKickerTag.exists(identity),
      showByline = trailMeta.showByline.exists(identity),
      imageCutoutReplace = trailMeta.imageCutoutReplace.exists(identity),
      showQuotedHeadline = trailMeta.showQuotedHeadline.exists(identity),
      imageSlideshowReplace = trailMeta.imageSlideshowReplace.exists(identity)
  )

  private[utils] def fromContent(content: Content, cardStyle: CardStyle): ResolvedMetaData =
    cardStyle match {
      case com.gu.facia.api.utils.Comment => Default.copy(
        showByline = true,
        showQuotedHeadline = true,
        imageCutoutReplace = true)
      case _ if isCartoonForContent(content) => Default.copy(showByline = true)
      case _ if isVideoForContent(content) => Default.copy(showMainVideo = true)
      case _ => Default
    }

  def fromContentAndTrailMetaData(content: Content, trailMeta: MetaDataCommonFields, cardStyle: CardStyle): ResolvedMetaData = {
    val metaDataFromContent = fromContent(content, cardStyle)
    metaDataFromContent.copy(
      isBreaking = trailMeta.isBreaking.getOrElse(metaDataFromContent.isBreaking),
      isBoosted = trailMeta.isBoosted.getOrElse(metaDataFromContent.isBoosted),
      imageHide = trailMeta.imageHide.getOrElse(metaDataFromContent.imageHide),
      imageReplace = trailMeta.imageReplace.getOrElse(metaDataFromContent.imageReplace),
      showKickerSection = trailMeta.showKickerSection.getOrElse(metaDataFromContent.showKickerSection),
      showKickerCustom = trailMeta.showKickerCustom.getOrElse(metaDataFromContent.showKickerCustom),
      showBoostedHeadline = trailMeta.showBoostedHeadline.getOrElse(metaDataFromContent.showBoostedHeadline),
      showMainVideo = trailMeta.showMainVideo.getOrElse(metaDataFromContent.showMainVideo),
      showKickerTag = trailMeta.showKickerTag.getOrElse(metaDataFromContent.showKickerTag),
      showByline = trailMeta.showByline.getOrElse(metaDataFromContent.showByline),
      imageCutoutReplace = trailMeta.imageCutoutReplace.getOrElse(metaDataFromContent.imageCutoutReplace),
      showQuotedHeadline = trailMeta.showQuotedHeadline.getOrElse(metaDataFromContent.showQuotedHeadline),
      imageSlideshowReplace = trailMeta.imageSlideshowReplace.getOrElse(metaDataFromContent.imageSlideshowReplace))}
}

case class ResolvedMetaData(
    isBreaking: Boolean,
    isBoosted: Boolean,
    imageHide: Boolean,
    imageReplace: Boolean,
    showKickerSection: Boolean,
    showKickerCustom: Boolean,
    showBoostedHeadline: Boolean,
    showMainVideo: Boolean,
    showKickerTag: Boolean,
    showByline: Boolean,
    imageCutoutReplace: Boolean,
    showQuotedHeadline: Boolean,
    imageSlideshowReplace: Boolean)

object ContentProperties {
  def fromResolvedMetaData(resolvedMetaData: ResolvedMetaData): ContentProperties =
    ContentProperties(
      isBreaking = resolvedMetaData.isBreaking,
      isBoosted = resolvedMetaData.isBoosted,
      imageHide = resolvedMetaData.imageHide,
      showBoostedHeadline = resolvedMetaData.showBoostedHeadline,
      showMainVideo = resolvedMetaData.showMainVideo,
      showKickerTag = resolvedMetaData.showKickerTag,
      showByline = resolvedMetaData.showByline,
      showQuotedHeadline = resolvedMetaData.showQuotedHeadline)
}

case class ContentProperties(
    isBreaking: Boolean,
    isBoosted: Boolean,
    imageHide: Boolean,
    showBoostedHeadline: Boolean,
    showMainVideo: Boolean,
    showKickerTag: Boolean,
    showByline: Boolean,
    showQuotedHeadline: Boolean)