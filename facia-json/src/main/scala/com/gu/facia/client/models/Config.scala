package com.gu.facia.client.models

import play.api.libs.json.Json

object CollectionConfigJson {
  implicit val jsonFormat = Json.format[CollectionConfigJson]

  val emptyConfig: CollectionConfigJson = withDefaults(None, None, None, None, None, None, None, None, None, None, None)

  def withDefaults(
    displayName: Option[String] = None,
    apiQuery: Option[String] = None,
    `type`: Option[String] = None,
    href: Option[String] = None,
    groups: Option[List[String]] = None,
    uneditable: Option[Boolean] = None,
    showTags: Option[Boolean] = None,
    showSections: Option[Boolean] = None,
    hideKickers: Option[Boolean] = None,
    showDateHeader: Option[Boolean] = None,
    showLatestUpdate: Option[Boolean] = None,
    excludeFromRss: Option[Boolean] = None,
    showTimestamps: Option[Boolean] = None
  ): CollectionConfigJson
    = CollectionConfigJson(
    displayName,
    apiQuery,
    `type`,
    href,
    groups,
    uneditable,
    showTags,
    showSections,
    hideKickers,
    showDateHeader,
    showLatestUpdate,
    excludeFromRss,
    showTimestamps
  )
}

case class CollectionConfigJson(
  displayName: Option[String],
  apiQuery: Option[String],
  `type`: Option[String],
  href: Option[String],
  groups: Option[List[String]],
  uneditable: Option[Boolean],
  showTags: Option[Boolean],
  showSections: Option[Boolean],
  hideKickers: Option[Boolean],
  showDateHeader: Option[Boolean],
  showLatestUpdate: Option[Boolean],
  excludeFromRss: Option[Boolean],
  showTimestamps: Option[Boolean]
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
  isHidden: Option[Boolean]
)

object ConfigJson {
  implicit val jsonFormat = Json.format[ConfigJson]
  def empty = ConfigJson(Map.empty, Map.empty)
}

case class ConfigJson(
  fronts: Map[String, FrontJson],
  collections: Map[String, CollectionConfigJson]
)


