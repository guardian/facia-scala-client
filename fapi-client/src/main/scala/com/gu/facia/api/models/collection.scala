package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.contentapi.LatestSnapsRequest
import com.gu.facia.api.utils.IntegerString
import com.gu.facia.client.models.{SupportingItem, Trail, CollectionJson}
import org.joda.time.DateTime

case class Collection(
  id: String,
  displayName: String,
  href: Option[String],
  live: List[Trail],
  draft: Option[List[Trail]],
  lastUpdated: Option[DateTime],
  updatedBy: Option[String],
  updatedEmail: Option[String],
  collectionConfig: CollectionConfig
)

object Collection {
  def fromCollectionJsonConfigAndContent(collectionId: String, collectionJson: Option[CollectionJson], collectionConfig: CollectionConfig): Collection = {
    Collection(
      collectionId,
      collectionJson.flatMap(_.displayName).orElse(collectionConfig.displayName).getOrElse("untitled"),
      collectionJson.flatMap(_.href).orElse(collectionConfig.href),
      collectionJson.map(_.live).getOrElse(Nil),
      collectionJson.flatMap(_.draft),
      collectionJson.map(_.lastUpdated),
      collectionJson.map(_.updatedBy),
      collectionJson.map(_.updatedEmail),
      collectionConfig)
  }

  def liveContent(collection: Collection, content: Set[Content], snapContent: Map[String, Option[Content]] = Map.empty): List[FaciaContent] = {
    // if content is not in the set it was most likely filtered out by the CAPI query, so exclude it
    // note that this does not currently deal with e.g. snaps
    def resolveTrail(trail: Trail): Option[FaciaContent] = {
      content.find(c => trail.id.endsWith("/" + c.safeFields.getOrElse("internalContentCode", throw new RuntimeException("No internal content code"))))
        .map { content =>
        trail.safeMeta.supporting
          .map(_.flatMap(resolveSupportingContent))
          .map(supportingItems => CuratedContent.fromTrailAndContentWithSupporting(content, trail.safeMeta, supportingItems, collection.collectionConfig))
          .getOrElse(CuratedContent.fromTrailAndContent(content, trail.safeMeta, collection.collectionConfig))}
        .orElse {
          snapContent
            .find{case (id, _) => trail.id == id}
            .map(c => LatestSnap.fromTrailAndContent(trail, c._2))}
        .orElse{ Snap.maybeFromTrail(trail)}}

    def resolveSupportingContent(supportingItem: SupportingItem): Option[FaciaContent] = {
      content
        .find(c => supportingItem.id.endsWith("/" + c.safeFields.getOrElse("internalContentCode", throw new RuntimeException("No internal content code"))))
        .map { content => SupportingCuratedContent.fromTrailAndContent(content, supportingItem.safeMeta, collection.collectionConfig)}
        .orElse {
          snapContent
            .find{case (id, _) => supportingItem.id == id}
            .map(c => LatestSnap.fromSupportingItemAndContent(supportingItem, c._2))}
        .orElse{ Snap.maybeFromSupportingItem(supportingItem)}}

    collection.live.flatMap(resolveTrail)
  }

  def liveIdsWithoutSnaps(collection: Collection): List[String] =
    collection.live.filterNot(_.isSnap).map(_.id)

  private def allSupportingItems(collection: Collection): List[SupportingItem] =
    collection.live.flatMap(_.meta).flatMap(_.supporting).flatten

  def liveSupportingIdsWithoutSnaps(collection: Collection): List[String] =
    allSupportingItems(collection).filterNot(_.isSnap).map(_.id)

  def liveSupportingSnaps(collection: Collection): LatestSnapsRequest =
    LatestSnapsRequest(
      allSupportingItems(collection)
      .filter(_.isSnap)
      .filter(_.safeMeta.snapType == Some("latest"))
      .flatMap(snap => snap.meta.flatMap(_.snapUri).map(uri => snap.id ->uri))
      .toMap)

  def latestSnapsRequestFor(collection: Collection): LatestSnapsRequest =
    LatestSnapsRequest(
      collection.live
      .filter(_.isSnap)
      .filter(_.safeMeta.snapType == Some("latest"))
      .flatMap(snap => snap.safeMeta.snapUri.map(uri => snap.id -> uri)).toMap)

  def withoutSnaps(collection: Collection): Collection = {
    collection.copy(
      live = collection.live.filterNot(_.isSnap),
      draft = collection.draft.map(_.filterNot(_.isSnap)))}
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