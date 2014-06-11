package com.gu.facia.client.models

import org.joda.time.DateTime
import play.api.libs.json.Json

object Trail {
  implicit val jsonFormat = Json.format[Trail]
}

case class Trail(
  id: String,
  frontPublicationDate: Int,
  meta: Map[String, String]
)

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
