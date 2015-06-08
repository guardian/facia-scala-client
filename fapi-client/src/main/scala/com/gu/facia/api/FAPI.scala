package com.gu.facia.api

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.Content
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
import com.gu.facia.api.contentapi.{ContentApi, LatestSnapsRequest}
import com.gu.facia.api.models._
import com.gu.facia.client.ApiClient
import com.gu.facia.client.models.TrailMetaData

import scala.concurrent.{ExecutionContext, Future}


object FAPI {
  def getFronts()(implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[Set[Front]] = {
    for {
      config <- Response.Async.Right(faciaClient.config)
    } yield Front.frontsFromConfig(config)
  }

  def frontForPath(path: String)(implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[Front] = {
    for {
      fronts <- getFronts
      front <- Response.fromOption(fronts.find(_.id == path), NotFound(s"Not front found for $path"))
    } yield front
  }

  /**
   * Fetches the collection information for the given id by resolving info out of the fronts config
   * and the collection's own config JSON.
   */
  def getCollection(collectionId: String)
                   (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[Collection] = {
    val fCollectionJson = faciaClient.collection(collectionId)
    val fConfigJson = faciaClient.config
    for {
      collectionJson <- Response.Async.Right(fCollectionJson)
      configJson <- Response.Async.Right(fConfigJson)
      collectionConfigJson <- Response.fromOption(configJson.collections.get(collectionId), NotFound(s"Collection config not found for $collectionId"))
      collectionConfig = CollectionConfig.fromCollectionJson(collectionConfigJson)
    } yield {
      Collection.fromCollectionJsonConfigAndContent(collectionId, collectionJson, collectionConfig)
    }
  }

  /**
   * Fetch all the collections for a front in one go
   */
  def frontCollections(frontId: String)
                      (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[List[Collection]] = {
    for {
      configJson <- Response.Async.Right(faciaClient.config)
      frontJson <- Response.fromOption(configJson.fronts.get(frontId), NotFound(s"No front found for $frontId"))
      collectionIds = frontJson.collections
      collectionsJsons <- Response.Async.Right(Future.traverse(collectionIds)(faciaClient.collection))
      collectionConfigJsons <- Response.traverse(
        collectionIds.map(id => Response.fromOption(configJson.collections.get(id), NotFound(s"Collection config not found for $id")))
      )
      collectionConfigs = collectionConfigJsons.map(CollectionConfig.fromCollectionJson)
    } yield {
      (collectionIds, collectionsJsons, collectionConfigs).zipped.toList.map { case (collectionId, collectionJson, collectionConfig) =>
        Collection.fromCollectionJsonConfigAndContent(collectionId, collectionJson, collectionConfig)
      }
    }
  }

  private def getLiveContentForCollection(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity)
                                   (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[Set[Content]] = {
    val itemIdsForRequest = Collection.liveIdsWithoutSnaps(collection)
    val supportingIdsForRequest = Collection.liveSupportingIdsWithoutSnaps(collection)
    val allItemIdsForRequest = itemIdsForRequest ::: supportingIdsForRequest
    for {
      hydrateQueries <- ContentApi.buildHydrateQueries(capiClient, allItemIdsForRequest, adjustSearchQuery)
      hydrateResponses <- ContentApi.getHydrateResponse(capiClient, hydrateQueries)
      content = ContentApi.itemsFromSearchResponses(hydrateResponses)}
      yield content
  }

  private def getDraftContentForCollection(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity)
                                   (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[Set[Content]] = {
    val itemIdsForRequest =
      Collection.draftIdsWithoutSnaps(collection)
        .getOrElse(Collection.liveIdsWithoutSnaps(collection))
    val supportingIdsForRequest =
      Collection.draftSupportingIdsWithoutSnaps(collection)
        .getOrElse(Collection.liveSupportingIdsWithoutSnaps(collection))
    val allItemIdsForRequest = itemIdsForRequest ::: supportingIdsForRequest
    for {
      hydrateQueries <- ContentApi.buildHydrateQueries(capiClient, allItemIdsForRequest, adjustSearchQuery)
      hydrateResponses <- ContentApi.getHydrateResponse(capiClient, hydrateQueries)
      content = ContentApi.itemsFromSearchResponses(hydrateResponses)}
      yield content
  }

  private def getLiveLatestSnapContentForCollection(collection: Collection, adjustItemQuery: AdjustItemQuery)
                      (implicit capiClient: GuardianContentClient, ec: ExecutionContext) = {
    val latestSnapsRequest: LatestSnapsRequest = Collection.liveLatestSnapsRequestFor(collection)
    val latestSupportingSnaps: LatestSnapsRequest = Collection.liveSupportingSnaps(collection)
    val allSnaps = latestSnapsRequest.join(latestSupportingSnaps)
    for(snapContent <- ContentApi.latestContentFromLatestSnaps(capiClient, allSnaps, adjustItemQuery))
      yield snapContent}

  private def getDraftLatestSnapContentForCollection(collection: Collection, adjustItemQuery: AdjustItemQuery)
                      (implicit capiClient: GuardianContentClient, ec: ExecutionContext) = {
    val latestSnapsRequest: LatestSnapsRequest =
      Collection.draftLatestSnapsRequestFor(collection)
        .getOrElse(Collection.liveLatestSnapsRequestFor(collection))
    val latestSupportingSnaps: LatestSnapsRequest =
      Collection.draftSupportingSnaps(collection)
        .getOrElse(Collection.liveSupportingSnaps(collection))
    val allSnaps = latestSnapsRequest.join(latestSupportingSnaps)
    for(snapContent <- ContentApi.latestContentFromLatestSnaps(capiClient, allSnaps, adjustItemQuery))
      yield snapContent}

  def getTreatsForCollection(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
                            (implicit capiClient: GuardianContentClient, ec: ExecutionContext) = {
    val (treatIds, treatsSnapsRequest) = Collection.treatsRequestFor(collection)
      for {
        hydrateQueries <- ContentApi.buildHydrateQueries(capiClient, treatIds, adjustSearchQuery)
        hydrateResponses <- ContentApi.getHydrateResponse(capiClient, hydrateQueries)
        snapContent <- ContentApi.latestContentFromLatestSnaps(capiClient, treatsSnapsRequest, adjustItemQuery)
        setOfContent = ContentApi.itemsFromSearchResponses(hydrateResponses)}
    yield Collection.treatContent(collection, setOfContent, snapContent)
  }

  def liveCollectionContentWithoutSnaps(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity)
                                (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    val collectionWithoutSnaps = Collection.withoutSnaps(collection)
    for(setOfContent <- getLiveContentForCollection(collection, adjustSearchQuery))
      yield Collection.liveContent(collectionWithoutSnaps, setOfContent)
  }

  def liveCollectionContentWithSnaps(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity, adjustSnapItemQuery: AdjustItemQuery = identity)
                                   (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    for {
      setOfContent <- getLiveContentForCollection(collection, adjustSearchQuery)
      snapContent <- getLiveLatestSnapContentForCollection(collection, adjustSnapItemQuery)}
    yield Collection.liveContent(collection, setOfContent, snapContent)}

  def draftCollectionContentWithoutSnaps(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity)
                                (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    val collectionWithoutSnaps = Collection.withoutSnaps(collection)
    for(setOfContent <- getDraftContentForCollection(collection, adjustSearchQuery))
      yield Collection.draftContent(collectionWithoutSnaps, setOfContent)
  }

  def draftCollectionContentWithSnaps(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity, adjustSnapItemQuery: AdjustItemQuery = identity)
                                   (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    for {
      setOfContent <- getDraftContentForCollection(collection, adjustSearchQuery)
      snapContent <- getDraftLatestSnapContentForCollection(collection, adjustSnapItemQuery)}
    yield Collection.draftContent(collection, setOfContent, snapContent)}

  /**
   * Fetches content for the given backfill query. The query can be manipulated for different
   * requirements by providing adjustment functions. The results then have their facia metadata
   * resolved using the collection information.
   */
  def backfill(backfillQuery: String, collection: Collection,
               adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
              (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[List[CuratedContent]] = {

    val query = ContentApi.buildBackfillQuery(backfillQuery)
      .right.map(adjustSearchQuery)
      .left.map(adjustItemQuery)
    val backfillResponse = ContentApi.getBackfillResponse(capiClient, query)
    for {
      backfillContent <- ContentApi.backfillContentFromResponse(backfillResponse)
    } yield {
      backfillContent.map(CuratedContent.fromTrailAndContent(_, TrailMetaData.empty, None, collection.collectionConfig))
    }
  }
}
