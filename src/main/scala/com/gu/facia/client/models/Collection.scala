package com.gu.facia.client.models

import org.joda.time.DateTime
import play.api.libs.json.Json

object Trail {
  implicit val jsonReads = Json.reads[Trail]
}

case class Trail(
  id: String,
  frontPublicationDate: Long,
  meta: Map[String, String]
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
