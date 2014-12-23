package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
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
  def fromCollectionJsonAndContent(collectionJson: CollectionJson, content: Map[String, Content]): Collection = {
    Collection(
      collectionJson.name.get, // TODO: Ask fronts team how to resolve this if it is None?
      collectionJson.live.map(trail => FaciaContent.fromTrailAndContent(trail, content.get(trail.id).get)),
      collectionJson.draft.map { _.map { trail =>
        FaciaContent.fromTrailAndContent(trail, content.get(trail.id).get)
      }},
      collectionJson.updatedBy,
      collectionJson.updatedEmail,
      collectionJson.displayName,
      collectionJson.href
    )
  }
}


