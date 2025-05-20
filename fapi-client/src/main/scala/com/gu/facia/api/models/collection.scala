package com.gu.facia.api.models

import com.gu.contentapi.client.ContentApiClient
import com.gu.contentapi.client.model.v1.{Content, ItemResponse}
import com.gu.contentatom.thrift.{Atom, AtomData}
import com.gu.contentatom.thrift.atom.media.MediaAtom
import com.gu.facia.api.contentapi.{ItemQueries, LatestSnapsRequest, LinkSnapsRequest}
import com.gu.facia.client.models.{CollectionJson, SupportingItem, TargetedTerritory, Trail}
import org.joda.time.{DateTime, DateTimeZone}
import com.gu.facia.api.utils.BoostLevel
import com.gu.facia.api.{CapiError, Response}

import scala.concurrent.{ExecutionContext, Future}


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
  collectionConfig: CollectionConfig,
  targetedTerritory: Option[TargetedTerritory]
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
      collectionConfig,
      collectionConfig.targetedTerritory)
  }


  private def isSplashCard(trail: Trail, index: Int, collectionType: String): Boolean = {
    (collectionType, trail.safeMeta.group, index) match {
      case ("flexible/general", Some("3"), _) => true
      case ("flexible/special", Some("1"), 0) => true
      case _ => false
    }
  }
  private[models] def maxSupportingItems(isSplashCard: Boolean, collectionType: String, boostLevel: String): Int = {
    if (
          (collectionType == "flexible/general" || collectionType == "flexible/special") &&
          !isSplashCard && boostLevel == BoostLevel.Default.label)
    {
       2
    } else {
      4
    }
  }

  def contentFrom(collection: Collection,
                  content: Set[Content],
                  snapContent: Map[String, Option[Content]] = Map.empty,
                  linkSnapBrandingsByEdition: Map[String, BrandingByEdition],
                  from: Collection => List[Trail]): List[FaciaContent] = {
    // if content is not in the set it was most likely filtered out by the CAPI query, so exclude it
    // note that this does not currently deal with e.g. snaps
    def resolveTrail(trail: Trail, index: Int): Option[FaciaContent] = {
      val boostLevel = trail.safeMeta.boostLevel
      val isSplash = isSplashCard(trail, index, collection.collectionConfig.collectionType)
      val maxItems =  maxSupportingItems(isSplash, collection.collectionConfig.collectionType, boostLevel.getOrElse("default") )

      content.find { c =>
        trail.id.endsWith("/" + c.fields.flatMap(_.internalPageCode).getOrElse(throw new RuntimeException("No internal page code")))
      }
        .map { content =>
        trail.safeMeta.supporting
          .map(_.flatMap(resolveSupportingContent))
          .map(supportingItems => CuratedContent.fromTrailAndContentWithSupporting(content, trail.safeMeta, Option(trail.frontPublicationDate), supportingItems.take(maxItems), collection.collectionConfig))
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

    for {
      (trail, index) <- from(collection).zipWithIndex
      content <- resolveTrail(trail, index)
    } yield content

  }

  def enrichContentWithVideo(faciaContent: List[FaciaContent])(implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {

    def getMediaAtomData(fcContent: FaciaContent)(implicit ec: ExecutionContext, capiClient: ContentApiClient): Response[Option[AtomData.Media]] = {
      val futureMaybeAtomData = fcContent match {
        case faciaContent if faciaContent.properties.videoReplace && faciaContent.atomId.isDefined =>
          val futureResponse = capiClient.getResponse(ContentApiClient.item(faciaContent.atomId.get))
          futureResponse.map { response =>
            resolveVideo(response) match {
              case Some(atom: AtomData.Media) if !isExpired(atom.media) => Some(atom)
              case _ => None
            }
          }

        case faciaContent: CuratedContent if faciaContent.properties.showMainVideo =>
          val maybeMainMediaAtomData = for {
            atoms <- faciaContent.content.atoms
            mediaAtoms <- atoms.media
            mainMediaAtom <- mediaAtoms.find(atom =>
              atom.data match {
                case AtomData.Media(media) => !isExpired(media)
                case _ => false
              }
            )
            mediaAtomData <- mainMediaAtom.data match {
              case m: AtomData.Media => Some(m)
              case _ => None
            }
          } yield mediaAtomData

          Future.successful(maybeMainMediaAtomData)
        case _ => Future.successful(None)
      }

      Response.Async.Right(futureMaybeAtomData) mapError { err =>
        CapiError(s"Failed to get mediaAtomData response ${err.message}", err.cause)
      }
    }

    def resolveVideo(response: ItemResponse): Option[AtomData] = {
      for {
        video <- response.media
        resolved <- Some(video.data)
      } yield resolved
    }

    def isExpired(mediaAtom: MediaAtom): Boolean = {
      val maybeExpired = for {
        metadata <- mediaAtom.metadata
        expiryDate <- metadata.expiryDate
      } yield new DateTime(expiryDate).withZone(DateTimeZone.UTC).isBeforeNow
      maybeExpired.getOrElse(false)
    }


    val responses: Seq[Response[FaciaContent]] = faciaContent.map {
      case curatedContent: CuratedContent =>
        getMediaAtomData(curatedContent).map(mediaAtomData => curatedContent.copy(mediaAtomData = mediaAtomData))
      case content => Response.Right(content)
    }

    Response.traverse(responses.toList)

  }

  /* Live Methods */
  def liveContent(
                   collection: Collection,
                   content: Set[Content],
                   snapContent: Map[String, Option[Content]] = Map.empty,
                   linkSnapBrandingsByEdition: Map[String, BrandingByEdition] = Map.empty
                 )(implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    val liveContent = contentFrom(collection, content, snapContent, linkSnapBrandingsByEdition, collection => collection.live)
    enrichContentWithVideo(liveContent)
  }

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
                  )(implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    val draftContent = contentFrom(collection,
      content,
      snapContent,
      linkSnapBrandingsByEdition,
      collection => collection.draft.getOrElse(collection.live))
    enrichContentWithVideo(draftContent)
  }

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
                   snapContent: Map[String, Option[Content]] = Map.empty)
                  (implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    val treatContent = contentFrom(collection,
      content,
      snapContent,
      linkSnapBrandingsByEdition = Map.empty,
      collection => collection.treats)
    enrichContentWithVideo(treatContent)
  }

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