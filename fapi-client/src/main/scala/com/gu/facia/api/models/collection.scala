package com.gu.facia.api.models

import com.gu.contentapi.client.model.v1.Content
import com.gu.facia.api.contentapi.{LatestSnapsRequest, LinkSnapsRequest}
import com.gu.facia.api.utils.IntegerString
import com.gu.facia.client.models.{CollectionJson, SupportingItem, Trail}
import org.joda.time.DateTime

case class Collection(
  id: String,
  displayName: String,
  href: Option[String],
  live: List[Trail],
  draft: Option[List[Trail]],
  treats: List[Trail],
  lastUpdated: Option[DateTime],
  updatedBy: Option[String],
  updatedEmail: Option[String],
  collectionConfig: CollectionConfig
)

object Collection {
  def fromCollectionJsonConfigAndContent(collectionId: String, collectionJson: Option[CollectionJson], collectionConfig: CollectionConfig): Collection = {
    Collection(
      collectionId,
      collectionConfig.displayName.orElse(collectionJson.flatMap(_.displayName)).getOrElse("untitled"),
      collectionConfig.href.orElse(collectionJson.flatMap(_.href)),
      collectionJson.map(_.live).getOrElse(Nil),
      collectionJson.flatMap(_.draft),
      collectionJson.flatMap(_.treats).getOrElse(Nil),
      collectionJson.map(_.lastUpdated),
      collectionJson.map(_.updatedBy),
      collectionJson.map(_.updatedEmail),
      collectionConfig)
  }

  def contentFrom(collection: Collection,
                  content: Set[Content],
                  snapContent: Map[String, Option[Content]] = Map.empty,
                  linkSnapBrandingsByEdition: Map[String, BrandingByEdition],
                  from: Collection => List[Trail]): List[FaciaContent] = {
    // if content is not in the set it was most likely filtered out by the CAPI query, so exclude it
    // note that this does not currently deal with e.g. snaps
    def resolveTrail(trail: Trail): Option[FaciaContent] = {
      content.find { c =>
        trail.id.endsWith("/" + c.fields.flatMap(_.internalPageCode).getOrElse(throw new RuntimeException("No internal page code")))
      }
        .map { content =>
        trail.safeMeta.supporting
          .map(_.flatMap(resolveSupportingContent))
          .map(supportingItems => CuratedContent.fromTrailAndContentWithSupporting(content, trail.safeMeta, Option(trail.frontPublicationDate), supportingItems, collection.collectionConfig))
          .getOrElse(CuratedContent.fromTrailAndContent(content, trail.safeMeta, Option(trail.frontPublicationDate), collection.collectionConfig))}
        .orElse {
          snapContent
            .find{case (id, _) => trail.id == id}
            .map(c => LatestSnap.fromTrailAndContent(trail, c._2))}
        .orElse {
          linkSnapBrandingsByEdition
          .find {
            case (id, _) => trail.id == id
          }.flatMap {
            case (_, brandingByEdition) => Snap.maybeFromTrailAndBrandings(trail, brandingByEdition)
          }
        .orElse { Snap.maybeFromTrail(trail) }
      }
    }

    def resolveSupportingContent(supportingItem: SupportingItem): Option[FaciaContent] = {
      content.find { c =>
        supportingItem.id.endsWith("/" + c.fields.flatMap(_.internalPageCode).getOrElse(throw new RuntimeException("No internal page code")))
      }
        .map { content => SupportingCuratedContent.fromTrailAndContent(content, supportingItem.safeMeta, supportingItem.frontPublicationDate, collection.collectionConfig)}
        .orElse {
          snapContent
            .find{case (id, _) => supportingItem.id == id}
            .map(c => LatestSnap.fromSupportingItemAndContent(supportingItem, c._2))}
        .orElse{ Snap.maybeFromSupportingItem(supportingItem)}}

    from(collection).flatMap(resolveTrail)
  }

  /* Live Methods */
  def liveContent(
    collection: Collection,
    content: Set[Content],
    snapContent: Map[String, Option[Content]] = Map.empty,
    linkSnapBrandingsByEdition: Map[String, BrandingByEdition] = Map.empty
  ): List[FaciaContent] =
    contentFrom(collection, content, snapContent, linkSnapBrandingsByEdition, collection => collection.live)

  def liveIdsWithoutSnaps(collection: Collection): List[String] =
    collection.live.filterNot(_.isSnap).map(_.id)

  private def allLiveSupportingItems(collection: Collection): List[SupportingItem] =
    collection.live.flatMap(_.meta).flatMap(_.supporting).flatten

  def liveSupportingIdsWithoutSnaps(collection: Collection): List[String] =
    allLiveSupportingItems(collection).filterNot(_.isSnap).map(_.id)

  def liveSupportingSnaps(collection: Collection): LatestSnapsRequest =
    LatestSnapsRequest(
      allLiveSupportingItems(collection)
        .filter(_.isSnap)
        .filter(_.safeMeta.snapType.contains("latest"))
        .flatMap(snap => snap.meta.flatMap(_.snapUri).map(uri => snap.id ->uri))
        .toMap)

  def liveLatestSnapsRequestFor(collection: Collection): LatestSnapsRequest =
    LatestSnapsRequest(
      collection.live
      .filter(_.isSnap)
      .filter(_.safeMeta.snapType.contains("latest"))
      .flatMap(snap => snap.safeMeta.snapUri.map(uri => snap.id -> uri))
      .toMap)

  private def linkSnapsRequestFor(trails: List[Trail]): LinkSnapsRequest = LinkSnapsRequest(
    trails.filter(_.isSnap)
    .filter(_.safeMeta.snapType.contains("link"))
    .flatMap(snap => snap.safeMeta.href.map(uri => snap.id -> uri))
    .toMap)

  def liveLinkSnapsRequestFor(collection: Collection): LinkSnapsRequest = linkSnapsRequestFor(collection.live)

  /* Draft Methods */
  def draftContent(
    collection: Collection,
    content: Set[Content],
    snapContent: Map[String, Option[Content]] = Map.empty,
    linkSnapBrandingsByEdition: Map[String, BrandingByEdition] = Map.empty
  ): List[FaciaContent] =
    contentFrom(collection,
      content,
      snapContent,
      linkSnapBrandingsByEdition,
      collection => collection.draft.getOrElse(collection.live))

  def draftIdsWithoutSnaps(collection: Collection): Option[List[String]] =
    collection.draft.map(_.filterNot(_.isSnap).map(_.id))

  private def allDraftSupportingItems(collection: Collection): Option[List[SupportingItem]] =
    collection.draft.map(_.flatMap(_.meta).flatMap(_.supporting).flatten)

  def draftSupportingIdsWithoutSnaps(collection: Collection): Option[List[String]] =
    allDraftSupportingItems(collection).map(_.filterNot(_.isSnap).map(_.id))

  def draftSupportingSnaps(collection: Collection): Option[LatestSnapsRequest] =
      allDraftSupportingItems(collection)
        .map( listOfSupportingItems =>
          LatestSnapsRequest(
            listOfSupportingItems.filter(_.isSnap)
              .filter(_.safeMeta.snapType.contains("latest"))
              .flatMap(snap => snap.meta.flatMap(_.snapUri).map(uri => snap.id ->uri))
              .toMap))

  def draftLatestSnapsRequestFor(collection: Collection): Option[LatestSnapsRequest] =
      collection.draft.map( listOfTrails =>
        LatestSnapsRequest(
          listOfTrails.filter(_.isSnap)
          .filter(_.safeMeta.snapType.contains("latest"))
          .flatMap(snap => snap.safeMeta.snapUri.map(uri => snap.id -> uri))
          .toMap))

  def draftLinkSnapsRequestFor(collection: Collection): Option[LinkSnapsRequest] =
    collection.draft map linkSnapsRequestFor

  /* Treats */
  def treatContent(collection: Collection,
    content: Set[Content],
    snapContent: Map[String, Option[Content]] = Map.empty): List[FaciaContent] =
    contentFrom(collection,
      content,
      snapContent,
      linkSnapBrandingsByEdition = Map.empty,
      collection => collection.treats)

  def treatsRequestFor(collection: Collection): (List[String], LatestSnapsRequest) = {
    val latestSnapsRequest =
      LatestSnapsRequest(
        collection.treats
          .filter(_.isSnap)
          .filter(_.safeMeta.snapType.contains("latest"))
          .flatMap(snap => snap.safeMeta.snapUri.map(uri => snap.id -> uri))
          .toMap)

    val treatIds = collection.treats.filterNot(_.isSnap).map(_.id)

    (treatIds, latestSnapsRequest)}

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