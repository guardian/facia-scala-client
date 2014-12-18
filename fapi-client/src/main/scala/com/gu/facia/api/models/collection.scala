package com.gu.facia.api.models

import com.gu.facia.client.models.CollectionJson

case class Collection(
  name: String,
  live: List[CuratedContent],
  draft: Option[List[CuratedContent]],
  updatedBy: String,
  updatedEmail: String,
  displayName: Option[String],
  href: Option[String])

object Collection {
  def fromCollectionJson(collectionJson: CollectionJson): Collection = ???
}


