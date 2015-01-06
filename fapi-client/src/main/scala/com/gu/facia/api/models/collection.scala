package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.IntegerString
import com.gu.facia.client.models.{Trail, CollectionJson}
import org.joda.time.DateTime

case class Collection(
  id: CollectionId,
  displayName: String,
  live: List[Trail],
  draft: Option[List[Trail]],
  lastUpdated: Option[DateTime],
  updatedBy: Option[String],
  updatedEmail: Option[String],
  href: Option[String],
  apiQuery: Option[String],
  collectionType: String,
  groups: Option[List[Group]],
  uneditable: Boolean,
  showTags: Boolean,
  showSections: Boolean,
  hideKickers: Boolean,
  showDateHeader: Boolean,
  showLatestUpdate: Boolean
)

object Collection {
  def fromCollectionJsonConfigAndContent(id: CollectionId, collectionJson: Option[CollectionJson], collectionConfig: CollectionConfig): Collection = {
    Collection(
      id,
      collectionJson.flatMap(_.displayName).orElse(collectionConfig.displayName).getOrElse("untitled"),
      collectionJson.map(_.live).getOrElse(Nil),
      collectionJson.flatMap(_.draft),
      collectionJson.map(_.lastUpdated),
      collectionJson.map(_.updatedBy),
      collectionJson.map(_.updatedEmail),
      collectionJson.flatMap(_.href).orElse(collectionConfig.href),
      collectionConfig.apiQuery,
      collectionConfig.collectionType,
      collectionConfig.groups.map(Group.fromGroups),
      collectionConfig.uneditable,
      collectionConfig.showTags,
      collectionConfig.showSections,
      collectionConfig.hideKickers,
      collectionConfig.showDateHeader,
      collectionConfig.showLatestUpdate
    )
  }

  def liveContent(collection: Collection, content: Set[Content]): List[CuratedContent] = {
    // if content is not in the set it was most likely filtered out by the CAPI query, so exclude it
    // note that this does not currently deal with e.g. snaps
    val collectionConfig = CollectionConfig.fromCollection(collection)
    collection.live.flatMap { trail =>
      val contentCodeUrl = trail.id
      content.find(c => trail.id.endsWith("/" + c.safeFields.getOrElse("internalContentCode", throw new RuntimeException("No internal content code")))).map { content =>
        FaciaContent.fromTrailAndContent(content, trail.safeMeta, collectionConfig)
      }
    }
  }
}

case class Group(get: Int)

object Group {
  def fromGroups(groups: Groups): List[Group] = {
    groups.groups.collect {
      case IntegerString(n) => Group(n)
    }
  }

  def toGroups(groups: List[Group]): Groups = {
    Groups(groups.map(_.get.toString))
  }
}