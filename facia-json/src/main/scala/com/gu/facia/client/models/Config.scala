package com.gu.facia.client.models

import com.sun.corba.se.spi.legacy.interceptor.UnknownType
import com.typesafe.scalalogging.StrictLogging
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

case object Branded extends Metadata

case object UnknownMetadata extends Metadata


object Metadata extends StrictLogging {

  val tags: Map[String, Metadata] = Map(
    ("Canonical", Canonical), ("Special", Special), ("Breaking", Breaking), ("Branded", Branded)
  )

  implicit object MetadataFormat extends Format[Metadata] {
    def reads(json: JsValue) = {
      (json \ "type").transform[JsString](Reads.JsStringReads) match {
        case JsSuccess(JsString(string), _) => tags.get(string) match {
          case Some(result) => JsSuccess(result)
          case None =>
            logger.warn(s"Could not convert CollectionTag: $string is of unknown type")
            JsSuccess(UnknownMetadata)
        }
        case _ => JsError("Could not convert CollectionTag: type is not a string")
      }
    }

    def writes(cardStyle: Metadata) = cardStyle match {
      case Canonical => JsObject(Seq("type" -> JsString("Canonical")))
      case Special => JsObject(Seq("type" -> JsString("Special")))
      case Breaking => JsObject(Seq("type" -> JsString("Breaking")))
      case Branded => JsObject(Seq("type" -> JsString("Branded")))
      case UnknownMetadata => JsObject(Seq("type" -> JsString("UnknownMetadata")))
    }
  }
}

sealed trait CollectionPlatform
case object AnyPlatform extends CollectionPlatform
case object WebCollection extends CollectionPlatform
case object AppCollection extends CollectionPlatform

object CollectionPlatform {
  implicit object CollectionPlatformFormat extends Format[CollectionPlatform] {
    def reads(json: JsValue) = json match {
      case JsString("Web") => JsSuccess(WebCollection)
      case JsString("App") => JsSuccess(AppCollection)
      case JsString("Any") => JsSuccess(AnyPlatform)
      case other => JsError(s"Could not deserialize to a CollectionPlatform type: $other")
    }

    def writes(platform: CollectionPlatform): JsString = platform match {
      case AnyPlatform => JsString("Any")
      case WebCollection => JsString("Web")
      case AppCollection => JsString("App")
    }
  }
}

object FrontsToolSettings {
  implicit val jsonFormat = Json.format[FrontsToolSettings]
}

case class FrontsToolSettings (
  displayEditWarning: Option[Boolean])

object DisplayHintsJson {
  implicit val jsonFormat = Json.format[DisplayHintsJson]
}

case class DisplayHintsJson(maxItemsToDisplay: Option[Int])

object CollectionConfigJson {
  implicit val jsonFormat = Json.format[CollectionConfigJson]

  val emptyConfig: CollectionConfigJson = withDefaults(None, None, None, None, None, None, None, None, None, None, None, None)

  def withDefaults(
    displayName: Option[String] = None,
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
    hideShowMore: Option[Boolean] = None,
    displayHints: Option[DisplayHintsJson] = None,
    userVisibility: Option[String] = None,
    platform: Option[CollectionPlatform] = None,
    frontsToolSettings: Option[FrontsToolSettings] = None
  ): CollectionConfigJson
    = CollectionConfigJson(
    displayName,
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
    hideShowMore,
    displayHints,
    userVisibility,
    platform,
    frontsToolSettings
  )
}

case class CollectionConfigJson(
  displayName: Option[String],
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
  hideShowMore: Option[Boolean],
  displayHints: Option[DisplayHintsJson],
  userVisibility: Option[String],
  platform: Option[CollectionPlatform],
  frontsToolSettings: Option[FrontsToolSettings]
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


