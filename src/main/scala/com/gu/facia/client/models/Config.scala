package com.gu.facia.client.models

import play.api.libs.json.Json

object CollectionConfig {
  implicit val jsonReads = Json.reads[CollectionConfig]
}

case class CollectionConfig(
  displayName: Option[String],
  apiQuery: Option[String],
  `type`: Option[String],
  href: Option[String],
  groups: Option[List[String]],
  uneditable: Option[Boolean],
  showTags: Option[Boolean],
  showSections: Option[Boolean]
)

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
}

case class Config(
  fronts: Map[String, Front],
  collections: Map[String, CollectionConfig]
)


