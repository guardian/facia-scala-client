package com.gu.facia.api.utils

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.models._
import com.gu.facia.client.models.{TrailMetaData, MetaDataCommonFields}


object ResolvedMetaData {
  val Cartoon = "type/cartoon"
  val Video = "type/video"
  val Comment = "tone/comment"

  private def fold[T](faciaContent: FaciaContent)(c: (CuratedContent) => T, scc: (SupportingCuratedContent) => T,
    ls: (LinkSnap) => T, las: (LatestSnap) => T): T = faciaContent match {
    case curatedContent: CuratedContent => c(curatedContent)
    case supportingCuratedContent: SupportingCuratedContent => scc(supportingCuratedContent)
    case linkSnap: LinkSnap => ls(linkSnap)
    case latestSnap: LatestSnap => las(latestSnap)}

  def isCartoonForContent(content: Content): Boolean =
    content.tags.exists(_.id == Cartoon)

  def isCartoon(faciaContent: FaciaContent): Boolean = {

    fold(faciaContent)(
      curatedContent => isCartoonForContent(curatedContent.content),
      supportingCuratedContent => isCartoonForContent(supportingCuratedContent.content),
      _ => false,
      latestSnap => latestSnap.latestContent.exists(isCartoonForContent))}

  def isCommentForContent(content: Content): Boolean =
    content.tags.exists(_.id == Comment)

  def isComment(faciaContent: FaciaContent): Boolean = {
    fold(faciaContent)(
      curatedContent => isCommentForContent(curatedContent.content),
      supportingCuratedContent => isCommentForContent(supportingCuratedContent.content),
      _ => false,
      latestSnap => latestSnap.latestContent.exists(isCommentForContent))}

  def isVideoForContent(content: Content): Boolean =
    content.tags.exists(_.id == Video)

  def isVideo(faciaContent: FaciaContent): Boolean = {

    fold(faciaContent)(
      curatedContent => isVideoForContent(curatedContent.content),
      supportingCuratedContent => isVideoForContent(supportingCuratedContent.content),
      _ => false,
      latestSnap => latestSnap.latestContent.exists(isVideoForContent))
  }

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
    showQuotedHeadline = false)

  private[utils] def fromTrailMetaData(trailMeta: MetaDataCommonFields): ResolvedMetaData =
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
      showQuotedHeadline = trailMeta.showQuotedHeadline.exists(identity))

  private[utils] def fromContent(content: Content): ResolvedMetaData = {
    val mutatingMetaDataDefaults = Default
    if (isCartoonForContent(content))
      mutatingMetaDataDefaults.copy(showByline = true)
    else if (isCommentForContent(content))
      mutatingMetaDataDefaults.copy(
        showByline = true,
        showQuotedHeadline = true,
        imageCutoutReplace = true)
    else if (isVideoForContent(content))
      mutatingMetaDataDefaults.copy(showMainVideo = true)
    else
      mutatingMetaDataDefaults}

  private [utils] def joinResolvedMetaDataAndTrailMetaData(first: ResolvedMetaData, second: MetaDataCommonFields): ResolvedMetaData =
    first.copy(
      isBreaking = second.isBreaking.getOrElse(first.isBreaking),
      isBoosted = second.isBoosted.getOrElse(first.isBoosted),
      imageHide = second.imageHide.getOrElse(first.imageHide),
      imageReplace = second.imageReplace.getOrElse(first.imageReplace),
      showKickerSection = second.showKickerSection.getOrElse(first.showKickerSection),
      showKickerCustom = second.showKickerCustom.getOrElse(first.showKickerCustom),
      showBoostedHeadline = second.showBoostedHeadline.getOrElse(first.showBoostedHeadline),
      showMainVideo = second.showMainVideo.getOrElse(first.showMainVideo),
      showKickerTag = second.showKickerTag.getOrElse(first.showKickerTag),
      showByline = second.showByline.getOrElse(first.showByline),
      imageCutoutReplace = second.imageCutoutReplace.getOrElse(first.imageCutoutReplace),
      showQuotedHeadline = second.showQuotedHeadline.getOrElse(first.showQuotedHeadline)
    )

  def fromContentAndTrailMetaData(content: Content, trailMeta: MetaDataCommonFields): ResolvedMetaData = {
    val metaDataFromContent = fromContent(content)
    joinResolvedMetaDataAndTrailMetaData(metaDataFromContent, trailMeta)
  }
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
    showQuotedHeadline: Boolean)