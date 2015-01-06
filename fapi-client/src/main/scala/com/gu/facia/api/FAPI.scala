package com.gu.facia.api

import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.contentapi.ContentApi
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
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
      collectionJsonOption <- Response.Async.Right(fCollectionJson)
      collectionJson <- Response.fromOption(collectionJsonOption, NotFound(s"Collection JSON not found for $collectionId"))
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
      collectionsJsonOptions <- Response.Async.Right(Future.traverse(collectionIds)(faciaClient.collection))
      collectionsJsons = collectionsJsonOptions.flatten
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

  def collectionContent(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity)
                       (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[List[CuratedContent]] = {
    val itemIdsForRequest = collection.live.filterNot(_.isSnap).map(_.id)

    ContentApi.buildHydrateQueries(capiClient, itemIdsForRequest, adjustSearchQuery) match {
      case Success(hydrateQueries) =>
        for {
          hydrateResponses <- ContentApi.getHydrateResponse(capiClient, hydrateQueries)
          content = ContentApi.itemsFromSearchResponses(hydrateResponses)
        } yield {
          Collection.liveContent(collection, content)
        }

      case Failure(error) =>
        Response.Left(UrlConstructError(error.getMessage, Some(error)))
    }
  }

  /**
   * Fetches content for the given backfill query. The query can be manipulated for different
   * requirements by providing adjustment functions. The results then have their facia metadata
   * resolved using the collection information.
   */
  def backfill(backfillQuery: String, collection: Collection,
               adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
              (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[List[CuratedContent]] = {

    val query = ContentApi.buildBackfillQuery(capiClient, backfillQuery)
      .right.map(adjustSearchQuery)
      .left.map(adjustItemQuery)
    for {
      backfillContent <- ContentApi.backfillContentFromResponse(ContentApi.getBackfillResponse(capiClient, query))
      collectionConfig = CollectionConfig.fromCollection(collection)
    } yield {
      backfillContent.map(FaciaContent.fromTrailAndContent(_, TrailMetaData.empty, collectionConfig))
    }
  }
}
