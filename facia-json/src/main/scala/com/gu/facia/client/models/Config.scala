package com.gu.facia.client.models

import play.api.libs.json._

object Backfill {
  implicit val jsonFormat = Json.format[Backfill]
}
case class Backfill(
  `type`: String,
  query: String
)

sealed trait Metadata

case object Canonical extends Metadata

case object Special extends Metadata

case object Breaking extends Metadata


object Metadata {

  val tags: Map[String, Metadata] = Map(("Canonical", Canonical), ("Special", Special), ("Breaking", Breaking))

  implicit object MetadataFormat extends Format[Metadata] {
    def reads(json: JsValue) = {
      (json \ "type").transform[JsString](Reads.JsStringReads) match {
        case JsSuccess(JsString(string), _) => tags.get(string) match {
          case Some(result) => JsSuccess(result)
          case _ => JsError("Could not convert CollectionTag: string is of unknown type")
        }
        case _ => JsError("Could not convert CollectionTag: type is not a string")
      }
    }

    def writes(cardStyle: Metadata) = cardStyle match {
      case Canonical => JsObject(Seq("type" -> JsString("Canonical")))
      case Special => JsObject(Seq("type" -> JsString("Special")))
      case Breaking => JsObject(Seq("type" -> JsString("Breaking")))
    }
  }
}


object CollectionConfigJson {
  implicit val jsonFormat = Json.format[CollectionConfigJson]

  val emptyConfig: CollectionConfigJson = withDefaults(None, None, None, None, None, None, None, None, None, None, None, None, None)

  def withDefaults(
    displayName: Option[String] = None,
    apiQuery: Option[String] = None,
    backfill: Option[Backfill] = None,
    metadata: Option[List[Metadata]] = None,
    `type`: Option[String] = None,
    href: Option[String] = None,
    description: Option[String] = None,
    groups: Option[List[String]] = None,
    uneditable: Option[Boolean] = None,
    showTags: Option[Boolean] = None,
    showSections: Option[Boolean] = None,
    hideKickers: Option[Boolean] = None,
    showDateHeader: Option[Boolean] = None,
    showLatestUpdate: Option[Boolean] = None,
    excludeFromRss: Option[Boolean] = None,
    showTimestamps: Option[Boolean] = None,
    hideShowMore: Option[Boolean] = None
  ): CollectionConfigJson
    = CollectionConfigJson(
    displayName,
    apiQuery,
    backfill,
    metadata,
    `type`,
    href,
    description,
    groups,
    uneditable,
    showTags,
    showSections,
    hideKickers,
    showDateHeader,
    showLatestUpdate,
    excludeFromRss,
    showTimestamps,
    hideShowMore
  )
}

case class CollectionConfigJson(
  displayName: Option[String],
  apiQuery: Option[String],
  backfill: Option[Backfill],
  metadata: Option[List[Metadata]],
  `type`: Option[String],
  href: Option[String],
  description: Option[String],
  groups: Option[List[String]],
  uneditable: Option[Boolean],
  showTags: Option[Boolean],
  showSections: Option[Boolean],
  hideKickers: Option[Boolean],
  showDateHeader: Option[Boolean],
  showLatestUpdate: Option[Boolean],
  excludeFromRss: Option[Boolean],
  showTimestamps: Option[Boolean],
  hideShowMore: Option[Boolean]
  ) {
  val collectionType = `type`
}

object FrontJson {
  implicit val jsonFormat = Json.format[FrontJson]
}

case class FrontJson(
  collections: List[String],
  navSection: Option[String],
  webTitle: Option[String],
  title: Option[String],
  description: Option[String],
  onPageDescription: Option[String],
  imageUrl: Option[String],
  imageWidth: Option[Int],
  imageHeight: Option[Int],
  isImageDisplayed: Option[Boolean],
  priority: Option[String],
  isHidden: Option[Boolean],
  canonical: Option[String],
  group: Option[String]
)

object ConfigJson {
  implicit val jsonFormat = Json.format[ConfigJson]
  def empty = ConfigJson(Map.empty, Map.empty)
}

case class ConfigJson(
  fronts: Map[String, FrontJson],
  collections: Map[String, CollectionConfigJson]
)


