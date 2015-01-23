package com.gu.facia.api

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.Content
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
import com.gu.facia.api.contentapi.{ContentApi, LatestSnapsRequest}
import com.gu.facia.api.models._
import com.gu.facia.client.ApiClient
import com.gu.facia.client.models.TrailMetaData

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


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

  private def getContentForCollection(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity)
                                   (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[Set[Content]] = {
    val itemIdsForRequest = Collection.liveIdsWithoutSnaps(collection)
    val supportingIdsForRequest = Collection.liveSublinkIdsWithoutSnaps(collection)
    ContentApi.buildHydrateQueries(capiClient, itemIdsForRequest ::: supportingIdsForRequest, adjustSearchQuery) match {
      case Success(hydrateQueries) =>
        for {
          hydrateResponses <- ContentApi.getHydrateResponse(capiClient, hydrateQueries)
          content = ContentApi.itemsFromSearchResponses(hydrateResponses)}
        yield content
      case Failure(error) =>
        Response.Left(UrlConstructError(error.getMessage, Some(error)))}}

  private def getLatestSnapContentForCollection(collection: Collection, adjustItemQuery: AdjustItemQuery)
                      (implicit capiClient: GuardianContentClient, ec: ExecutionContext) = {
    val latestSnapsRequest: LatestSnapsRequest = Collection.latestSnapsRequestFor(collection)
    val latestSublinkSnaps: LatestSnapsRequest = Collection.liveSublinkSnaps(collection)
    val snaps = LatestSnapsRequest(latestSnapsRequest.snaps ++ latestSublinkSnaps.snaps)
    for(snapContent <- ContentApi.latestContentFromLatestSnaps(capiClient, snaps, adjustItemQuery))
      yield snapContent}

  def collectionContentWithoutSnaps(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity)
                                (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    for(setOfContent <- getContentForCollection(collection, adjustSearchQuery))
      yield Collection.liveContent(collection, setOfContent)
  }

  def collectionContentWithSnaps(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity, adjustSnapItemQuery: AdjustItemQuery = identity)
                                   (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    for {
      setOfContent <- getContentForCollection(collection, adjustSearchQuery)
      snapContent <- getLatestSnapContentForCollection(collection, adjustSnapItemQuery)}
    yield Collection.liveContent(collection, setOfContent, snapContent)}

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
      collectionConfig = CollectionConfig.fromCollection(collection)
    } yield {
      backfillContent.map(CuratedContent.fromTrailAndContent(_, TrailMetaData.empty, collectionConfig))
    }
  }
}
