package com.gu.facia.api.utils

import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.{FAPI, Response}
import com.gu.facia.api.contentapi.ContentApi
import com.gu.facia.api.contentapi.ContentApi._
import com.gu.facia.api.models.{FaciaContent, CollectionConfig, CuratedContent}
import com.gu.facia.client.ApiClient
import com.gu.facia.client.models.{CollectionJson, Backfill, TrailMetaData}

import scala.concurrent.ExecutionContext

object BackfillContent {
  def resolveFromConfig(collectionConfig: CollectionConfig): BackfillResolver = {
    collectionConfig.backfill match {
      case Some(backfill: Backfill) => backfill.`type` match {
        case "capi" => CapiBackfill(backfill.query, collectionConfig)
        case "collection" => CollectionBackfill(backfill.query)
        case _ => throw new Error(s"Invalid backfill type ${backfill.`type`}")
      }
      case None => collectionConfig.apiQuery match {
        case Some(query: String) => CapiBackfill(query, collectionConfig)
        case None => EmptyBackfill()
      }
    }
  }
}

sealed trait BackfillResolver {
  def backfill(adjustSearchQuery: AdjustSearchQuery, adjustItemQuery: AdjustItemQuery)
              (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[List[FaciaContent]]
}

case class CapiBackfill(query: String, collectionConfig: CollectionConfig) extends BackfillResolver {
  def backfill(adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
              (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {

    val capiQuery = ContentApi.buildBackfillQuery(query)
      .right.map(adjustSearchQuery)
      .left.map(adjustItemQuery)
    val backfillResponse = ContentApi.getBackfillResponse(capiClient, capiQuery)
    for {
      backfillContent <- ContentApi.backfillContentFromResponse(backfillResponse)
    } yield {
      backfillContent.map(CuratedContent.fromTrailAndContent(_, TrailMetaData.empty, None, collectionConfig))
    }
  }
}

case class CollectionBackfill(parentCollectionId: String) extends BackfillResolver {
  def backfill(adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
              (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    for {
      collection <- FAPI.getCollection(parentCollectionId)
      curatedCollection <- FAPI.liveCollectionContentWithSnaps(collection, adjustSearchQuery, adjustItemQuery)
    } yield curatedCollection
  }
}

case class EmptyBackfill() extends BackfillResolver {
  def backfill(adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
              (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = Response.Right(Nil)
}
