package com.gu.facia.api

import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.contentapi.ContentApi
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
import com.gu.facia.api.models._
import com.gu.facia.client.ApiClient
import com.gu.facia.client.models.TrailMetaData

import scala.concurrent.ExecutionContext


object FAPI {
  def getFronts()(implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[Set[Front]] = {
    for {
      config <- Response.Async.Right(faciaClient.config)
    } yield {
      config.fronts
        .map { case (id, json) => Front.fromFrontJson(id, json)}
        .toSet
    }
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
  def getCollection(id: String, adjustSearchQuery: AdjustSearchQuery = identity)
                   (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[Collection] = {
    val fCollectionJson = faciaClient.collection(id)
    val fConfigJson = faciaClient.config
    for {
      // make Facia calls
      collectionJson <- Response.Async.Right(fCollectionJson)
      configJson <- Response.Async.Right(fConfigJson)

      // get collection config
      collectionConfigJson <- Response.fromOption(configJson.collections.get(id), NotFound(s"Collection config not found for $id"))
      collectionConfig = CollectionConfig.fromCollectionJson(collectionConfigJson)

      // ask CAPI for details on content
      itemIds = collectionJson.live.map(_.id)
      hydrateQuery = adjustSearchQuery(ContentApi.buildHydrateQuery(capiClient, itemIds))
      hydrateResponse <- ContentApi.getHydrateResponse(capiClient, hydrateQuery)
      content = ContentApi.itemsFromSearchResponse(hydrateResponse)
    } yield Collection.fromCollectionJsonConfigAndContent(collectionJson, collectionConfig, content)
  }

  /**
   * Fetches content for the given backfill query. The query can be manipulated for different
   * requirements by providing adjustment functions. The results then have their facia metadata
   * resolved using the collection information.
   */
  def backfill(backfillQuery: String, collectionId: CollectionId, collection: Collection,
               adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
              (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[List[CuratedContent]] = {

    val query = ContentApi.buildBackfillQuery(capiClient, backfillQuery)
      .right.map(adjustSearchQuery)
      .left.map(adjustItemQuery)

    val fBackfillResponse = ContentApi.getBackfillResponse(capiClient, query)
    val fConfigJson = faciaClient.config
    for {
      backfillContent <- ContentApi.backfillContentFromResponse(fBackfillResponse)
      configJson <- Response.Async.Right(fConfigJson)
      collectionConfigJson <- Response.fromOption(configJson.collections.get(collectionId.id), NotFound(s"Collection config not found for ${collectionId.id}"))
    } yield {
      val collectionConfig = CollectionConfig.fromCollectionJson(collectionConfigJson)
      backfillContent.map(FaciaContent.fromTrailAndContent(_, TrailMetaData.empty, collectionConfig))
    }
  }
}
