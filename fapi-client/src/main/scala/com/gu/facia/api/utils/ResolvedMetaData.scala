package com.gu.facia.api.utils

import com.fasterxml.jackson.databind.`type`.CollectionType
import com.gu.facia.client.models.MetaDataCommonFields
import play.api.libs.json.{Format, Json}
import com.gu.contentapi.client.model.v1.{Content, Element, ElementType}
import com.gu.facia.api.models.CollectionConfig

sealed trait AspectRatio {
  def label: String
}

object AspectRatio {
  case object Portrait45 extends AspectRatio {
    val label = "4:5"
  }

  case object Landscape53 extends AspectRatio {
    val label = "5:3"
  }

  case object Landscape54 extends AspectRatio {
    val label = "5:4"
  }

  case object Square extends AspectRatio {
    val label = "1:1"
  }

  val Landscape54Collections = List(
    "flexible/special",
    "flexible/general",
    "scrollable/small",
    "scrollable/medium",
    "static/medium/4",
  )

  val PortraitCollections = List(
    "scrollable/feature",
    "static/feature/2",
  )

  def getAspectRatio(collectionType: String): AspectRatio = {
    collectionType match {
      case _ if PortraitCollections.contains(collectionType) => Portrait45
      case _ if Landscape54Collections.contains(collectionType) => Landscape54
      case "scrollable/highlights" => Square
      case _ => Landscape53
    }
  }

}

sealed trait BoostLevel {
  def label: String
}

object BoostLevel {
  case object Default extends BoostLevel {
    val label = "default"
  }
  case object Boost extends BoostLevel {
    val label = "boost"
  }
  case object MegaBoost extends BoostLevel {
    val label = "megaboost"
  }
  case object GigaBoost extends BoostLevel {
    val label = "gigaboost"
  }
  def fromMetaData(value: String): BoostLevel = value match {
    case GigaBoost.label => GigaBoost
    case MegaBoost.label => MegaBoost
    case Boost.label => Boost
    case _ => Default
  }
}

object ResolvedMetaData {
  implicit val resolvedMetaDataFormat: Format[ResolvedMetaData] = Json.format[ResolvedMetaData]

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
    boostLevel = BoostLevel.Default.label,
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
    imageSlideshowReplace = false,
    aspectRatio = AspectRatio.Landscape53.label)

  def fromTrailMetaData(trailMeta: MetaDataCommonFields): ResolvedMetaData =
    ResolvedMetaData(
      isBreaking = trailMeta.isBreaking.exists(identity),
      isBoosted = trailMeta.isBoosted.exists(identity),
      boostLevel = trailMeta.boostLevel.getOrElse(BoostLevel.Default.label),
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
      imageSlideshowReplace = trailMeta.imageSlideshowReplace.exists(identity),
      aspectRatio = trailMeta.aspectRatio.getOrElse(AspectRatio.Landscape53.label)

  )

  def fromContent(content: Content, cardStyle: CardStyle): ResolvedMetaData =
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
      boostLevel = trailMeta.boostLevel.getOrElse(BoostLevel.Default.label),
      imageHide = trailMeta.imageHide.getOrElse(metaDataFromContent.imageHide),
      imageReplace = trailMeta.imageReplace.getOrElse(metaDataFromContent.imageReplace),
      showKickerSection = trailMeta.showKickerSection.getOrElse(metaDataFromContent.showKickerSection),
      showKickerCustom = trailMeta.showKickerCustom.getOrElse(metaDataFromContent.showKickerCustom),
      showBoostedHeadline = trailMeta.showBoostedHeadline.getOrElse(metaDataFromContent.showBoostedHeadline),
      showMainVideo =  trailMeta.showMainVideo.getOrElse(metaDataFromContent.showMainVideo),
      showLivePlayable = trailMeta.showLivePlayable.getOrElse(metaDataFromContent.showLivePlayable),
      showKickerTag = trailMeta.showKickerTag.getOrElse(metaDataFromContent.showKickerTag),
      showByline = trailMeta.showByline.getOrElse(metaDataFromContent.showByline),
      imageCutoutReplace = trailMeta.imageCutoutReplace.getOrElse(metaDataFromContent.imageCutoutReplace),
      showQuotedHeadline = trailMeta.showQuotedHeadline.getOrElse(metaDataFromContent.showQuotedHeadline),
      imageSlideshowReplace = trailMeta.imageSlideshowReplace.getOrElse(metaDataFromContent.imageSlideshowReplace))}

  def toMap(resolvedMetaData: ResolvedMetaData, collectionConfig: CollectionConfig): Map[String, Boolean] = resolvedMetaData match {
    case ResolvedMetaData(
      isBreaking,
      isBoosted,
      boostLevel,
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
      imageSlideshowReplace,
      aspectRatio) =>
      Map(
        "isBreaking" -> isBreaking,
        "isBoosted" -> isBoosted,
        "boostLevel.default" -> (BoostLevel.fromMetaData(boostLevel) == BoostLevel.Default),
        "boostLevel.boost" -> (BoostLevel.fromMetaData(boostLevel) == BoostLevel.Boost),
        "boostLevel.megaboost" -> (BoostLevel.fromMetaData(boostLevel) == BoostLevel.MegaBoost),
        "boostLevel.gigaBoost" -> (BoostLevel.fromMetaData(boostLevel) == BoostLevel.GigaBoost),
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
        "imageSlideshowReplace" -> imageSlideshowReplace,
        "aspectRatio.landscape53" -> (AspectRatio.getAspectRatio(collectionConfig.collectionType) == AspectRatio.Landscape53),
        "aspectRatio.landscape54" -> (AspectRatio.getAspectRatio(collectionConfig.collectionType) == AspectRatio.Landscape54),
        "aspectRatio.portrait45" -> (AspectRatio.getAspectRatio(collectionConfig.collectionType) == AspectRatio.Portrait45),
        "aspectRatio.square" -> (AspectRatio.getAspectRatio(collectionConfig.collectionType) == AspectRatio.Square)
      )
  }
}

case class ResolvedMetaData(
    isBreaking: Boolean,
    isBoosted: Boolean,
    boostLevel: String,
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
    imageSlideshowReplace: Boolean,
    aspectRatio: String)

object ContentProperties {
  def fromResolvedMetaData(resolvedMetaData: ResolvedMetaData, config: CollectionConfig): ContentProperties =
    ContentProperties(
      isBreaking = resolvedMetaData.isBreaking,
      isBoosted = resolvedMetaData.isBoosted,
      boostLevel = BoostLevel.fromMetaData(resolvedMetaData.boostLevel),
      imageHide = resolvedMetaData.imageHide,
      showBoostedHeadline = resolvedMetaData.showBoostedHeadline,
      showMainVideo = resolvedMetaData.showMainVideo,
      showLivePlayable = resolvedMetaData.showLivePlayable,
      showKickerTag = resolvedMetaData.showKickerTag,
      showByline = resolvedMetaData.showByline,
      showQuotedHeadline = resolvedMetaData.showQuotedHeadline,
      imageSlideshowReplace = resolvedMetaData.imageSlideshowReplace,
      aspectRatio = AspectRatio.getAspectRatio(config.collectionType))
}

case class ContentProperties(
    isBreaking: Boolean,
    isBoosted: Boolean,
    boostLevel: BoostLevel,
    imageHide: Boolean,
    showBoostedHeadline: Boolean,
    showMainVideo: Boolean,
    showLivePlayable: Boolean,
    showKickerTag: Boolean,
    showByline: Boolean,
    showQuotedHeadline: Boolean,
    imageSlideshowReplace: Boolean,
    aspectRatio: AspectRatio)