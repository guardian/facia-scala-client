package com.gu.facia.client.models

import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

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
  val showKickerTag: Option[Boolean]
  val showKickerSection: Option[Boolean]
  val byline: Option[String]
  val showByline: Option[Boolean]
}

object SupportingItemMetaData {
  implicit val jsonFormat = Json.format[SupportingItemMetaData]

  val empty = SupportingItemMetaData(Map.empty)
}

case class SupportingItemMetaData(json: Map[String, JsValue]) extends MetaDataCommonFields {
  lazy val headline: Option[String] = json.get("headline").flatMap(_.asOpt[String])
  lazy val href: Option[String] = json.get("href").flatMap(_.asOpt[String])
  lazy val snapType: Option[String] = json.get("snapType").flatMap(_.asOpt[String])
  lazy val snapCss: Option[String] = json.get("snapCss").flatMap(_.asOpt[String])
  lazy val snapUri: Option[String] = json.get("snapUri").flatMap(_.asOpt[String])
  lazy val trailText: Option[String] = json.get("trailText").flatMap(_.asOpt[String])
  lazy val group: Option[String] = json.get("group").flatMap(_.asOpt[String])
  lazy val imageAdjust: Option[String] = json.get("imageAdjust").flatMap(_.asOpt[String])
  lazy val imageSrc: Option[String] = json.get("imageSrc").flatMap(_.asOpt[String])
  lazy val imageSrcWidth: Option[String] = json.get("imageSrcWidth").flatMap(_.asOpt[String])
  lazy val imageSrcHeight: Option[String] = json.get("imageSrcHeight").flatMap(_.asOpt[String])
  lazy val isBreaking: Option[Boolean] = json.get("isBreaking").flatMap(_.asOpt[Boolean])
  lazy val isBoosted: Option[Boolean] = json.get("isBoosted").flatMap(_.asOpt[Boolean])
  lazy val imageHide: Option[Boolean] = json.get("imageHide").flatMap(_.asOpt[Boolean])
  lazy val imageReplace: Option[Boolean] = json.get("imageReplace").flatMap(_.asOpt[Boolean])
  lazy val showMainVideo: Option[Boolean] = json.get("showMainVideo").flatMap(_.asOpt[Boolean])
  lazy val showKickerTag: Option[Boolean] = json.get("showKickerTag").flatMap(_.asOpt[Boolean])
  lazy val showKickerSection: Option[Boolean] = json.get("showKickerSection").flatMap(_.asOpt[Boolean])
  lazy val byline: Option[String] = json.get("byline").flatMap(_.asOpt[String])
  lazy val showByline: Option[Boolean] = json.get("showByline").flatMap(_.asOpt[Boolean])
  lazy val customKicker: Option[String] = json.get("customKicker").flatMap(_.asOpt[String])
  lazy val showCustomKicker: Option[Boolean] = json.get("customKicker").flatMap(_.asOpt[Boolean])
}

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

  val empty = TrailMetaData(Map.empty)

  def withDefaults(defaults: (String, JsValue)*): TrailMetaData =
    TrailMetaData(defaults.foldLeft(Map.empty[String, JsValue]){case (m, kv) => m + kv})
}

case class TrailMetaData(json: Map[String, JsValue]) extends MetaDataCommonFields {
  lazy val headline: Option[String] = json.get("headline").flatMap(_.asOpt[String])
  lazy val href: Option[String] = json.get("href").flatMap(_.asOpt[String])
  lazy val snapType: Option[String] = json.get("snapType").flatMap(_.asOpt[String])
  lazy val snapCss: Option[String] = json.get("snapCss").flatMap(_.asOpt[String])
  lazy val snapUri: Option[String] = json.get("snapUri").flatMap(_.asOpt[String])
  lazy val trailText: Option[String] = json.get("trailText").flatMap(_.asOpt[String])
  lazy val group: Option[String] = json.get("group").flatMap(_.asOpt[String])
  lazy val imageAdjust: Option[String] = json.get("imageAdjust").flatMap(_.asOpt[String])
  lazy val imageSrc: Option[String] = json.get("imageSrc").flatMap(_.asOpt[String])
  lazy val imageSrcWidth: Option[String] = json.get("imageSrcWidth").flatMap(_.asOpt[String])
  lazy val imageSrcHeight: Option[String] = json.get("imageSrcHeight").flatMap(_.asOpt[String])
  lazy val isBreaking: Option[Boolean] = json.get("isBreaking").flatMap(_.asOpt[Boolean])
  lazy val isBoosted: Option[Boolean] = json.get("isBoosted").flatMap(_.asOpt[Boolean])
  lazy val imageHide: Option[Boolean] = json.get("imageHide").flatMap(_.asOpt[Boolean])
  lazy val imageReplace: Option[Boolean] = json.get("imageReplace").flatMap(_.asOpt[Boolean])
  lazy val showMainVideo: Option[Boolean] = json.get("showMainVideo").flatMap(_.asOpt[Boolean])
  lazy val showKickerTag: Option[Boolean] = json.get("showKickerTag").flatMap(_.asOpt[Boolean])
  lazy val showKickerSection: Option[Boolean] = json.get("showKickerSection").flatMap(_.asOpt[Boolean])
  lazy val byline: Option[String] = json.get("byline").flatMap(_.asOpt[String])
  lazy val showByline: Option[Boolean] = json.get("showByline").flatMap(_.asOpt[Boolean])
  lazy val customKicker: Option[String] = json.get("customKicker").flatMap(_.asOpt[String])
  lazy val showCustomKicker: Option[Boolean] = json.get("customKicker").flatMap(_.asOpt[Boolean])
  lazy val supporting: Option[List[SupportingItem]] = json.get("suppoerting").flatMap(_.asOpt[List[SupportingItem]])
}

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
