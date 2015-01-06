package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.IntegerString
import com.gu.facia.client.models.{Trail, CollectionJson}
import org.joda.time.DateTime

case class Collection(
  id: String,
  displayName: String,
  live: List[Trail],
  draft: Option[List[Trail]],
  lastUpdated: DateTime,
  updatedBy: String,
  updatedEmail: String,
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
  def fromCollectionJsonConfigAndContent(collectionId: String, collectionJson: CollectionJson, collectionConfig: CollectionConfig): Collection = {
    Collection(
      collectionId,
      collectionJson.displayName.orElse(collectionConfig.displayName).getOrElse("untitled"),
      collectionJson.live,
      collectionJson.draft,
      collectionJson.lastUpdated,
      collectionJson.updatedBy,
      collectionJson.updatedEmail,
      collectionJson.href.orElse(collectionConfig.href),
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