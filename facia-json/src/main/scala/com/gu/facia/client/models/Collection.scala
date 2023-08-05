package com.gu.facia.client.models

import play.api.libs.json._
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import com.gu.facia.client.json.JodaFormat._

case class SlideshowAsset(src: String, width: String, height: String, caption: Option[String] = None)
object SlideshowAsset {
  implicit val slideshowAssetFormat: OFormat[SlideshowAsset] = Json.format[SlideshowAsset]
}

case class ImageSourceAsset(src: String, origin: String, width: String, height: String)
object ImageSourceAsset {
  implicit val imageSourceAssetFormat: OFormat[ImageSourceAsset] = Json.format[ImageSourceAsset]
}

sealed trait MetaDataCommonFields {
  val json: Map[String, JsValue]

  lazy val headline: Option[String] = json.get("headline").flatMap(_.asOpt[String])
  lazy val href: Option[String] = json.get("href").flatMap(_.asOpt[String])
  lazy val snapType: Option[String] = json.get("snapType").flatMap(_.asOpt[String])
  lazy val snapCss: Option[String] = json.get("snapCss").flatMap(_.asOpt[String])
  lazy val snapUri: Option[String] = json.get("snapUri").flatMap(_.asOpt[String])
  lazy val trailText: Option[String] = json.get("trailText").flatMap(_.asOpt[String])
  lazy val group: Option[String] = json.get("group").flatMap(_.asOpt[String])
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
  lazy val showKickerCustom: Option[Boolean] = json.get("showKickerCustom").flatMap(_.asOpt[Boolean])
  lazy val imageCutoutReplace: Option[Boolean] = json.get("imageCutoutReplace").flatMap(_.asOpt[Boolean])
  lazy val imageCutoutSrc: Option[String] = json.get("imageCutoutSrc").flatMap(_.asOpt[String])
  lazy val imageCutoutSrcWidth: Option[String] = json.get("imageCutoutSrcWidth").flatMap(_.asOpt[String])
  lazy val imageCutoutSrcHeight: Option[String] = json.get("imageCutoutSrcHeight").flatMap(_.asOpt[String])
  lazy val showBoostedHeadline: Option[Boolean] = json.get("showBoostedHeadline").flatMap(_.asOpt[Boolean])
  lazy val showQuotedHeadline: Option[Boolean] = json.get("showQuotedHeadline").flatMap(_.asOpt[Boolean])
  lazy val excludeFromRss: Option[Boolean] = json.get("excludeFromRss").flatMap(_.asOpt[Boolean])
  lazy val imageSlideshowReplace: Option[Boolean] = json.get("imageSlideshowReplace").flatMap(_.asOpt[Boolean])
  lazy val slideshow: Option[List[SlideshowAsset]] =
    json.get("slideshow").flatMap(_.asOpt[List[SlideshowAsset]]).filter(_.nonEmpty)
  lazy val showLivePlayable: Option[Boolean] = json.get("showLivePlayable").flatMap(_.asOpt[Boolean])
  lazy val imageSource: Option[ImageSourceAsset] = json.get("imageSource").flatMap(_.asOpt[ImageSourceAsset])
  lazy val hideShowMore: Option[Boolean] = json.get("hideShowMore").flatMap(_.asOpt[Boolean])
  lazy val atomId: Option[String] = json.get("atomId").flatMap(_.asOpt[String])
  lazy val blockId: Option[String] = json.get("blockId").flatMap(_.asOpt[String])
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
  implicit val jsonFormat: OFormat[SupportingItem] = Json.format[SupportingItem]
}

case class SupportingItem(
  id: String,
  frontPublicationDate: Option[Long],
  publishedBy: Option[String],
  meta: Option[SupportingItemMetaData]
) {
  val isSnap: Boolean = id.startsWith("snap/")
  lazy val safeMeta = meta.getOrElse(TrailMetaData.empty)
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
  implicit val jsonFormat: OFormat[Trail] = Json.format[Trail]
}

case class Trail(
  id: String,
  frontPublicationDate: Long,
  publishedBy: Option[String],
  meta: Option[TrailMetaData]
) {
  val isSnap: Boolean = id.startsWith("snap/")
  lazy val safeMeta = meta.getOrElse(TrailMetaData.empty)
}

object CollectionJson {
  implicit val jsonFormat: OFormat[CollectionJson] = Json.format[CollectionJson]
}

case class CollectionJson(
  live: List[Trail],
  draft: Option[List[Trail]],
  treats: Option[List[Trail]],
  lastUpdated: DateTime,
  updatedBy: String,
  updatedEmail: String,
  displayName: Option[String],
  href: Option[String],
  previously: Option[List[Trail]],
  targetedTerritory: Option[TargetedTerritory]

)
