package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.contentapi.LatestSnapsRequest
import com.gu.facia.api.utils.IntegerString
import com.gu.facia.client.models.{Trail, CollectionJson}
import org.joda.time.DateTime

case class Collection(
  id: String,
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
  def fromCollectionJsonConfigAndContent(collectionId: String, collectionJson: Option[CollectionJson], collectionConfig: CollectionConfig): Collection = {
    Collection(
      collectionId,
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

  def liveContent(collection: Collection, content: Set[Content], snapContent: Map[String, Option[Content]] = Map.empty): List[FaciaContent] = {
    // if content is not in the set it was most likely filtered out by the CAPI query, so exclude it
    // note that this does not currently deal with e.g. snaps
    val collectionConfig = CollectionConfig.fromCollection(collection)
    collection.live.flatMap { trail =>
      content.find(c => trail.id.endsWith("/" + c.safeFields.getOrElse("internalContentCode", throw new RuntimeException("No internal content code")))).map { content =>
        CuratedContent.fromTrailAndContent(content, trail.safeMeta, collectionConfig)
      }.orElse{
        snapContent.find{case (id, _) => trail.id == id}
        .map(c => LatestSnap(trail.id, trail.safeMeta.snapUri, trail.safeMeta.snapCss, c._2))
      }.orElse{ Snap.maybeFromTrail(trail) }
    }
  }

  def liveIdsWithoutSnaps(collection: Collection): List[String] =
    collection.live.filterNot(_.isSnap).map(_.id)

  def latestSnapsRequestFor(collection: Collection): LatestSnapsRequest =
    LatestSnapsRequest(
      collection.live
      .filter(_.isSnap)
      .filter(_.safeMeta.snapType == Some("latest"))
      .flatMap(snap => snap.safeMeta.snapUri.map(uri => snap.id -> uri)).toMap)
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