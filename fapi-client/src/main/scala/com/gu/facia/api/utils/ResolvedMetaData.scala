package com.gu.facia.api.utils

import java.io.PrintWriter

import com.gu.contentapi.client.model.v1.{Content, Element, ElementType}
import com.gu.facia.client.models.MetaDataCommonFields
import play.api.libs.json.{Format, Json}


object ResolvedMetaData {
  def writeToFile(string: String) = {
    Some(new PrintWriter("/etc/fapi-log.txt")).foreach{p => p.append(string); p.close}
  }
  implicit val resolvedMetaDataFormat: Format[ResolvedMetaData] = Json.format[ResolvedMetaData]

  val Cartoon = "type/cartoon"
  val Video = "type/video"
  val Comment = "tone/comment"

  def isCartoonForContent(content: Content): Boolean =
    content.tags.exists(_.id == Cartoon)

  def isCommentForContent(content: Content): Boolean =
    content.tags.exists(_.id == Comment)

  def isVideoForContent(content: Content): Boolean = {
    content.tags.exists(_.id == Video)
  }

  def isVideoElement(content: Content): Boolean = {
    writeToFile("IN IS VIDEO ELEMENT")
    content.tags.exists(_.id == Video) &&
    content.elements.flatMap(
      _.find { element =>
        element.`type` == ElementType.Video && element.relation == "main"
      }).isDefined
  }


  def isVideoAtom(content: Content): Boolean = {
    content.tags.exists(_.id == Video) &&
    content.elements.flatMap(
      _.find { element =>
        element.`type` == ElementType.Contentatom && element.relation == "main"
      }).isDefined
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
    showLivePlayable = false,
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
      showLivePlayable = trailMeta.showLivePlayable.exists(identity),
      showKickerTag = trailMeta.showKickerTag.exists(identity),
      showByline = trailMeta.showByline.exists(identity),
      imageCutoutReplace = trailMeta.imageCutoutReplace.exists(identity),
      showQuotedHeadline = trailMeta.showQuotedHeadline.exists(identity),
      imageSlideshowReplace = trailMeta.imageSlideshowReplace.exists(identity)
  )

  def fromContent(content: Content, cardStyle: CardStyle): ResolvedMetaData =
    cardStyle match {
      case com.gu.facia.api.utils.Comment => Default.copy(
        showByline = true,
        showQuotedHeadline = true,
        imageCutoutReplace = true)
      case _ if isCartoonForContent(content) => Default.copy(showByline = true)
      case _ if isVideoElement(content) => Default.copy(showMainVideo = false)
      case _ if isVideoAtom(content) => Default.copy(showMainVideo = false)
      case _ => Default
    }

  def fromContentAndTrailMetaData(content: Content, trailMeta: MetaDataCommonFields, cardStyle: CardStyle): ResolvedMetaData = {


    writeToFile("trailMeta.showMainVideo: " + trailMeta.showMainVideo.toString)
    val metaDataFromContent = fromContent(content, cardStyle)
    val x = metaDataFromContent.copy(
      isBreaking = trailMeta.isBreaking.getOrElse(metaDataFromContent.isBreaking),
      isBoosted = trailMeta.isBoosted.getOrElse(metaDataFromContent.isBoosted),
      imageHide = trailMeta.imageHide.getOrElse(metaDataFromContent.imageHide),
      imageReplace = trailMeta.imageReplace.getOrElse(metaDataFromContent.imageReplace),
      showKickerSection = trailMeta.showKickerSection.getOrElse(metaDataFromContent.showKickerSection),
      showKickerCustom = trailMeta.showKickerCustom.getOrElse(metaDataFromContent.showKickerCustom),
      showBoostedHeadline = trailMeta.showBoostedHeadline.getOrElse(metaDataFromContent.showBoostedHeadline),
      showMainVideo = getShowMainVideo(trailMeta.showMainVideo, metaDataFromContent.showMainVideo, content),
      showLivePlayable = trailMeta.showLivePlayable.getOrElse(metaDataFromContent.showLivePlayable),
      showKickerTag = trailMeta.showKickerTag.getOrElse(metaDataFromContent.showKickerTag),
      showByline = trailMeta.showByline.getOrElse(metaDataFromContent.showByline),
      imageCutoutReplace = trailMeta.imageCutoutReplace.getOrElse(metaDataFromContent.imageCutoutReplace),
      showQuotedHeadline = trailMeta.showQuotedHeadline.getOrElse(metaDataFromContent.showQuotedHeadline),
      imageSlideshowReplace = trailMeta.imageSlideshowReplace.getOrElse(metaDataFromContent.imageSlideshowReplace))
    writeToFile("METADATA FROM CONTENT "+metaDataFromContent.toString)
  writeToFile("fromContentAndTrailMetaData result " + x.toString)
    x


  }

  def getShowMainVideo(trailShowVideo: Option[Boolean], default: Boolean, content: Content): Boolean = {
    if (isVideoAtom(content)) false else trailShowVideo.getOrElse(default)
  }

  def toMap(resolvedMetaData: ResolvedMetaData): Map[String, Boolean] = resolvedMetaData match {
    case ResolvedMetaData(
      isBreaking,
      isBoosted,
      imageHide,
      imageReplace,
      showKickerSection,
      showKickerCustom,
      showBoostedHeadline,
      showMainVideo,
      showLivePlayable,
      showKickerTag,
      showByline,
      imageCutoutReplace,
      showQuotedHeadline,
      imageSlideshowReplace) =>
      Map(
        "isBreaking" -> isBreaking,
        "isBoosted" -> isBoosted,
        "imageHide" -> imageHide,
        "imageReplace" -> imageReplace,
        "showKickerSection" -> showKickerSection,
        "showKickerCustom" -> showKickerCustom,
        "showBoostedHeadline" -> showBoostedHeadline,
        "showMainVideo" -> showMainVideo,
        "showLivePlayable" -> showLivePlayable,
        "showKickerTag" -> showKickerTag,
        "showByline" -> showByline,
        "imageCutoutReplace" -> imageCutoutReplace,
        "showQuotedHeadline" -> showQuotedHeadline,
        "imageSlideshowReplace" -> imageSlideshowReplace)
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
    showLivePlayable: Boolean,
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
      showLivePlayable = resolvedMetaData.showLivePlayable,
      showKickerTag = resolvedMetaData.showKickerTag,
      showByline = resolvedMetaData.showByline,
      showQuotedHeadline = resolvedMetaData.showQuotedHeadline,
      imageSlideshowReplace = resolvedMetaData.imageSlideshowReplace)
}

case class ContentProperties(
    isBreaking: Boolean,
    isBoosted: Boolean,
    imageHide: Boolean,
    showBoostedHeadline: Boolean,
    showMainVideo: Boolean,
    showLivePlayable: Boolean,
    showKickerTag: Boolean,
    showByline: Boolean,
    showQuotedHeadline: Boolean,
    imageSlideshowReplace: Boolean)