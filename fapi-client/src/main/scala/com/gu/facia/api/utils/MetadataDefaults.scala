package com.gu.facia.api.utils

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.models._


object MetadataDefaults {
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

  val Default = MetadataDefaults(
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

  def fromContent(content: Content): MetadataDefaults = {
    if (isCartoonForContent(content))
      Default.copy(showByline = true)
    else if (isCommentForContent(content))
      Default.copy(
        showByline = true,
        showQuotedHeadline = true,
        imageCutoutReplace = true)
    else if (isVideoForContent(content))
      Default.copy(showMainVideo = true)
    else
      Default}
}

case class MetadataDefaults(
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