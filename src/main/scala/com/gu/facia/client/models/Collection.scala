package com.gu.facia.client.models

import play.api.libs.json._
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

object MetaFields {
  val headline: String = "headline"
  val href: String = "href"
  val snapType: String = "snapType"
  val snapCss: String = "snapCss"
  val snapUri: String = "snapUri"
  val trailText: String = "trailText"
  val group: String = "group"
  val imageSrc: String = "imageSrc"
  val imageSrcWidth: String = "imageSrcWidth"
  val imageSrcHeight: String = "imageSrcHeight"
  val isBreaking: String = "isBreaking"
  val isBoosted: String = "isBoosted"
  val imageHide: String = "imageHide"
  val imageReplace: String = "imageReplace"
  val showMainVideo: String = "showMainVideo"
  val showKickerTag: String = "showKickerTag"
  val showKickerSection: String = "showKickerSection"
  val byline: String = "byline"
  val showByline: String = "showByline"
  val customKicker: String = "customKicker"
  val showKickerCustom: String = "showKickerCustom"
  val imageCutoutReplace: String = "imageCutoutReplace"
  val imageCutoutSrc: String = "imageCutoutSrc"
  val imageCutoutSrcWidth: String = "imageCutoutSrcWidth"
  val imageCutoutSrcHeight: String = "imageCutoutSrcHeight"
  val showBoostedHeadline: String = "showBoostedHeadline"
  val showQuotedHeadline: String = "showQuotedHeadline"
}

sealed trait MetaDataCommonFields {
  val json: Map[String, JsValue]

  lazy val headline: Option[String] = json.get(MetaFields.headline).flatMap(_.asOpt[String])
  lazy val href: Option[String] = json.get(MetaFields.href).flatMap(_.asOpt[String])
  lazy val snapType: Option[String] = json.get(MetaFields.snapType).flatMap(_.asOpt[String])
  lazy val snapCss: Option[String] = json.get(MetaFields.snapCss).flatMap(_.asOpt[String])
  lazy val snapUri: Option[String] = json.get(MetaFields.snapUri).flatMap(_.asOpt[String])
  lazy val trailText: Option[String] = json.get(MetaFields.trailText).flatMap(_.asOpt[String])
  lazy val group: Option[String] = json.get(MetaFields.group).flatMap(_.asOpt[String])
  lazy val imageSrc: Option[String] = json.get(MetaFields.imageSrc).flatMap(_.asOpt[String])
  lazy val imageSrcWidth: Option[String] = json.get(MetaFields.imageSrcWidth).flatMap(_.asOpt[String])
  lazy val imageSrcHeight: Option[String] = json.get(MetaFields.imageSrcHeight).flatMap(_.asOpt[String])
  lazy val isBreaking: Option[Boolean] = json.get(MetaFields.isBreaking).flatMap(_.asOpt[Boolean])
  lazy val isBoosted: Option[Boolean] = json.get(MetaFields.isBoosted).flatMap(_.asOpt[Boolean])
  lazy val imageHide: Option[Boolean] = json.get(MetaFields.imageHide).flatMap(_.asOpt[Boolean])
  lazy val imageReplace: Option[Boolean] = json.get(MetaFields.imageReplace).flatMap(_.asOpt[Boolean])
  lazy val showMainVideo: Option[Boolean] = json.get(MetaFields.showMainVideo).flatMap(_.asOpt[Boolean])
  lazy val showKickerTag: Option[Boolean] = json.get(MetaFields.showKickerTag).flatMap(_.asOpt[Boolean])
  lazy val showKickerSection: Option[Boolean] = json.get(MetaFields.showKickerSection).flatMap(_.asOpt[Boolean])
  lazy val byline: Option[String] = json.get(MetaFields.byline).flatMap(_.asOpt[String])
  lazy val showByline: Option[Boolean] = json.get(MetaFields.showByline).flatMap(_.asOpt[Boolean])
  lazy val customKicker: Option[String] = json.get(MetaFields.customKicker).flatMap(_.asOpt[String])
  lazy val showKickerCustom: Option[Boolean] = json.get(MetaFields.showKickerCustom).flatMap(_.asOpt[Boolean])
  lazy val imageCutoutReplace: Option[Boolean] = json.get(MetaFields.imageCutoutReplace).flatMap(_.asOpt[Boolean])
  lazy val imageCutoutSrc: Option[String] = json.get(MetaFields.imageCutoutSrc).flatMap(_.asOpt[String])
  lazy val imageCutoutSrcWidth: Option[String] = json.get(MetaFields.imageCutoutSrcWidth).flatMap(_.asOpt[String])
  lazy val imageCutoutSrcHeight: Option[String] = json.get(MetaFields.imageCutoutSrcHeight).flatMap(_.asOpt[String])
  lazy val showBoostedHeadline: Option[Boolean] = json.get(MetaFields.showBoostedHeadline).flatMap(_.asOpt[Boolean])
  lazy val showQuotedHeadline: Option[Boolean] = json.get(MetaFields.showQuotedHeadline).flatMap(_.asOpt[Boolean])
}

object SupportingItemMetaData {
  implicit val flatReads: Reads[SupportingItemMetaData] = new Reads[SupportingItemMetaData] {
    override def reads(j: JsValue): JsResult[SupportingItemMetaData] = {
      JsSuccess(SupportingItemMetaData(j.asOpt[Map[String, JsValue]].getOrElse(Map.empty)))
    }
  }

  implicit val flatWrites: Writes[SupportingItemMetaData] = new Writes[SupportingItemMetaData] {
    override def writes(o: SupportingItemMetaData): JsValue = Json.toJson(o.json)
  }

  val empty = SupportingItemMetaData(Map.empty)
}

case class SupportingItemMetaData(json: Map[String, JsValue]) extends MetaDataCommonFields

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
  implicit val flatReads: Reads[TrailMetaData] = new Reads[TrailMetaData] {
    override def reads(j: JsValue): JsResult[TrailMetaData] = {
      JsSuccess(TrailMetaData(j.asOpt[Map[String, JsValue]].getOrElse(Map.empty)))
    }
  }

  implicit val flatWrites: Writes[TrailMetaData] = new Writes[TrailMetaData] {
    override def writes(o: TrailMetaData): JsValue = Json.toJson(o.json)
  }

  val empty = TrailMetaData(Map.empty)

  def withDefaults(defaults: (String, JsValue)*): TrailMetaData =
    TrailMetaData(defaults.foldLeft(Map.empty[String, JsValue]){case (m, kv) => m + kv})
}

case class TrailMetaData(json: Map[String, JsValue]) extends MetaDataCommonFields {
  lazy val supporting: Option[List[SupportingItem]] = json.get("supporting").flatMap(_.asOpt[List[SupportingItem]])
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
