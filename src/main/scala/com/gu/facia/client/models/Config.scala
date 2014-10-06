package com.gu.facia.client.models

import play.api.libs.json.Json

object CollectionConfig {
  implicit val jsonFormat = Json.format[CollectionConfig]

  val emptyConfig: CollectionConfig = withDefaults(None, None, None, None, None, None, None, None)

  def withDefaults(displayName: Option[String] = None, apiQuery: Option[String] = None,
            `type`: Option[String] = None, href: Option[String] = None, groups: Option[List[String]] = None,
            uneditable: Option[Boolean] = None, showTags: Option[Boolean] = None,
            showSections: Option[Boolean] = None, hideKickers: Option[Boolean] = None): CollectionConfig
    = CollectionConfig(displayName, apiQuery, `type`, href, groups, uneditable, showTags, showSections, hideKickers)
}

case class CollectionConfig(
  displayName: Option[String],
  apiQuery: Option[String],
  `type`: Option[String],
  href: Option[String],
  groups: Option[List[String]],
  uneditable: Option[Boolean],
  showTags: Option[Boolean],
  showSections: Option[Boolean],
  hideKickers: Option[Boolean]
  ) {
  val collectionType = `type`
}

object Front {
  implicit val jsonFormat = Json.format[Front]
}

case class Front(
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

object Config {
  implicit val jsonFormat = Json.format[Config]

  def empty = Config(Map.empty, Map.empty)
}

case class Config(
  fronts: Map[String, Front],
  collections: Map[String, CollectionConfig]
)


