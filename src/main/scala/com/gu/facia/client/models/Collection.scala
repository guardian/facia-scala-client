package com.gu.facia.client.models

import org.joda.time.DateTime
import play.api.libs.json.Json

object SupportingItemMetaData {
  implicit val jsonReads = Json.reads[SupportingItemMetaData]
}

case class SupportingItemMetaData(
  headline: Option[String],
  imageAdjust: Option[String],
  group: Option[String]
)

object SupportingItem {
  implicit val jsonReads = Json.reads[SupportingItem]
}

case class SupportingItem(
  id: String,
  meta: Option[SupportingItemMetaData]
)

object TrailMetaData {
  implicit val jsonReads = Json.reads[TrailMetaData]
}

case class TrailMetaData(
  headline: Option[String],
  imageAdjust: Option[String],
  group: Option[String],
  supporting: Option[List[SupportingItem]]
)

object Trail {
  implicit val jsonReads = Json.reads[Trail]
}

case class Trail(
  id: String,
  frontPublicationDate: Long,
  meta: TrailMetaData
)

object Collection {
  implicit val jsonReads = Json.reads[Collection]
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
