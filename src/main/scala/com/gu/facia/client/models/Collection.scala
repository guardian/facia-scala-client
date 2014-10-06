package com.gu.facia.client.models

import org.joda.time.DateTime
import play.api.libs.json.Json

case class Kicker(
                   customKicker: Option[String],
                   showCustomKicker: Option[Boolean],
                   showKickerTag: Option[Boolean],
                   showKickerSection: Option[Boolean])

object Kicker {
  implicit val kickerFormat = Json.format[Kicker]
}

sealed trait MetaDataCommonFields {
  val headline: Option[String]
  val href: Option[String]
  val snapType: Option[String]
  val snapCss: Option[String]
  val snapUri: Option[String]
  val trailText: Option[String]
  val group: Option[String]
  val imageAdjust: Option[String]
  val imageSrc: Option[String]
  val imageSrcWidth: Option[String]
  val imageSrcHeight: Option[String]
  val isBreaking: Option[Boolean]
  val isBoosted: Option[Boolean]
  val imageHide: Option[Boolean]
  val imageReplace: Option[Boolean]
  val showMainVideo: Option[Boolean]
  val byline: Option[String]
  val showByline: Option[Boolean]
  val kicker: Option[Kicker]
}

object SupportingItemMetaData {
  implicit val jsonFormat = Json.format[SupportingItemMetaData]

  val empty = SupportingItemMetaData(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )
}

case class SupportingItemMetaData(
  headline: Option[String],
  href: Option[String],
  snapType: Option[String],
  snapCss: Option[String],
  snapUri: Option[String],
  trailText: Option[String],
  group: Option[String],
  imageAdjust: Option[String],
  imageSrc: Option[String],
  imageSrcWidth: Option[String],
  imageSrcHeight: Option[String],
  isBreaking: Option[Boolean],
  isBoosted: Option[Boolean],
  imageHide: Option[Boolean],
  imageReplace: Option[Boolean],
  showMainVideo: Option[Boolean],
  byline: Option[String],
  showByline: Option[Boolean],
  kicker: Option[Kicker]
) extends MetaDataCommonFields

object SupportingItem {
  implicit val jsonFormat = Json.format[SupportingItem]
}

case class SupportingItem(
  id: String,
  frontPublicationDate: Option[Long],
  meta: Option[SupportingItemMetaData]
) {
  val isSnap: Boolean = id.startsWith("snap/")
}

object TrailMetaData {
  implicit val jsonFormat = Json.format[TrailMetaData]

  val empty = TrailMetaData(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )
}

case class TrailMetaData(
  headline: Option[String],
  href: Option[String],
  snapType: Option[String],
  snapCss: Option[String],
  snapUri: Option[String],
  trailText: Option[String],
  group: Option[String],
  imageAdjust: Option[String],
  imageSrc: Option[String],
  imageSrcWidth: Option[String],
  imageSrcHeight: Option[String],
  isBreaking: Option[Boolean],
  isBoosted: Option[Boolean],
  imageHide: Option[Boolean],
  imageReplace: Option[Boolean],
  supporting: Option[List[SupportingItem]],
  showMainVideo: Option[Boolean],
  byline: Option[String],
  showByline: Option[Boolean],
  kicker: Option[Kicker]
) extends MetaDataCommonFields

object Trail {
  implicit val jsonFormat = Json.format[Trail]
}

case class Trail(
  id: String,
  frontPublicationDate: Long,
  meta: Option[TrailMetaData]
) {
  val isSnap: Boolean = id.startsWith("snap/")
  lazy val safeMeta = meta.getOrElse(TrailMetaData.empty)
}

object Collection {
  implicit val jsonFormat = Json.format[Collection]
}

case class Collection(
  name: Option[String],
  live: List[Trail],
  draft: Option[List[Trail]],
  lastUpdated: DateTime,
  updatedBy: String,
  updatedEmail: String,
  displayName: Option[String],
  href: Option[String]
)
