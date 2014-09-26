package com.gu.facia.client.models

import play.api.libs.json.Json

object CollectionConfig {
  implicit val jsonReads = Json.reads[CollectionConfig]

  val emptyConfig = CollectionConfig("", None, None, None, None, None, None, None, None)

  def apply(id: String = "", displayName: Option[String] = None, apiQuery: Option[String] = None,
            `type`: Option[String] = None, href: Option[String] = None, groups: Option[List[String]] = None,
            uneditable: Option[Boolean] = None, showTags: Option[Boolean] = None,
            showSections: Option[Boolean] = None): CollectionConfig
    = CollectionConfig(id, displayName, apiQuery, `type`, href, groups, uneditable, showTags, showSections)
}

case class CollectionConfig(
  id: String,
  displayName: Option[String],
  apiQuery: Option[String],
  `type`: Option[String],
  href: Option[String],
  groups: Option[List[String]],
  uneditable: Option[Boolean],
  showTags: Option[Boolean],
  showSections: Option[Boolean]
) {
  val collectionType = `type`
}

object Front {
  implicit val jsonReads = Json.reads[Front]
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
  priority: Option[String]
)

object Config {
  implicit val jsonReads = Json.reads[Config]

  def empty = Config(Map.empty, Map.empty)
}

case class Config(
  fronts: Map[String, Front],
  collections: Map[String, CollectionConfig]
)


