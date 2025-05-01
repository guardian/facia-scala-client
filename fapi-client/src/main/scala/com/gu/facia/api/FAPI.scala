package com.gu.facia.api

import com.gu.contentapi.client.ContentApiClient
import com.gu.contentapi.client.model.v1.Content
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
import com.gu.facia.api.contentapi.{ContentApi, LatestSnapsRequest, LinkSnapsRequest}
import com.gu.facia.api.models._
import com.gu.facia.api.utils.{BackfillResolver, BoostLevel, ContentProperties}
import com.gu.facia.client.ApiClient

import scala.PartialFunction.condOpt
import scala.collection.compat._
import scala.concurrent.{ExecutionContext, Future}


object FAPI {
  def getFronts()(implicit capiClient: ContentApiClient, faciaClient: ApiClient, ec: ExecutionContext): Response[Set[Front]] = {
    for {
      config <- Response.Async.Right(faciaClient.config)
    } yield Front.frontsFromConfig(config)
  }

  def frontForPath(path: String)(implicit capiClient: ContentApiClient, faciaClient: ApiClient, ec: ExecutionContext): Response[Front] = {
    for {
      fronts <- getFronts()
      front <- Response.fromOption(fronts.find(_.id == path), NotFound(s"Not front found for $path"))
    } yield front
  }

  /**
   * Fetches the collection information for the given id by resolving info out of the fronts config
   * and the collection's own config JSON.
   */
  def getCollection(collectionId: String)
                   (implicit capiClient: ContentApiClient, faciaClient: ApiClient, ec: ExecutionContext): Response[Collection] = {
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
                      (implicit capiClient: ContentApiClient, faciaClient: ApiClient, ec: ExecutionContext): Response[List[Collection]] = {
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
      collectionIds.lazyZip(collectionsJsons).lazyZip(collectionConfigs).toList.map { case (collectionId, collectionJson, collectionConfig) =>
        Collection.fromCollectionJsonConfigAndContent(collectionId, collectionJson, collectionConfig)
      }
    }
  }

  private def getLiveContentForCollection(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity)
                                   (implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[Set[Content]] = {
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
                                   (implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[Set[Content]] = {
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
                      (implicit capiClient: ContentApiClient, ec: ExecutionContext) = {
    val latestSnapsRequest: LatestSnapsRequest = Collection.liveLatestSnapsRequestFor(collection)
    val latestSupportingSnaps: LatestSnapsRequest = Collection.liveSupportingSnaps(collection)
    val allSnaps = latestSnapsRequest.join(latestSupportingSnaps)
    for(snapContent <- ContentApi.latestContentFromLatestSnaps(capiClient, allSnaps, adjustItemQuery))
      yield snapContent}

  private def getDraftLatestSnapContentForCollection(collection: Collection, adjustItemQuery: AdjustItemQuery)
                      (implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[Map[String, Option[Content]]] = {
    val latestSnapsRequest: LatestSnapsRequest =
      Collection.draftLatestSnapsRequestFor(collection)
        .getOrElse(Collection.liveLatestSnapsRequestFor(collection))
    val latestSupportingSnaps: LatestSnapsRequest =
      Collection.draftSupportingSnaps(collection)
        .getOrElse(Collection.liveSupportingSnaps(collection))
    val allSnaps = latestSnapsRequest.join(latestSupportingSnaps)
    for(snapContent <- ContentApi.latestContentFromLatestSnaps(capiClient, allSnaps, adjustItemQuery))
      yield snapContent}

  private def getLiveLinkSnapBrandingsForCollection(collection: Collection)
    (
      implicit capiClient: ContentApiClient,
      ec: ExecutionContext
    ): Response[Map[String, BrandingByEdition]] =
    getLinkSnapBrandings(Collection.liveLinkSnapsRequestFor(collection))

  private def getDraftLinkSnapBrandingsForCollection(collection: Collection)
    (
      implicit capiClient: ContentApiClient,
      ec: ExecutionContext
    ): Response[Map[String, BrandingByEdition]] =
    getLinkSnapBrandings(
      Collection.draftLinkSnapsRequestFor(collection).getOrElse(Collection.liveLinkSnapsRequestFor(collection))
    )

  private def getLinkSnapBrandings(request: LinkSnapsRequest)(
    implicit capiClient: ContentApiClient, ec: ExecutionContext
  ): Response[Map[String, BrandingByEdition]] =
    for (snapContent <- ContentApi.linkSnapBrandingsByEdition(capiClient, request)) yield snapContent

  def getTreatsForCollection(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
                            (implicit capiClient: ContentApiClient, ec: ExecutionContext) = {
    val (treatIds, treatsSnapsRequest) = Collection.treatsRequestFor(collection)
      for {
        hydrateQueries <- ContentApi.buildHydrateQueries(capiClient, treatIds, adjustSearchQuery)
        hydrateResponses <- ContentApi.getHydrateResponse(capiClient, hydrateQueries)
        snapContent <- ContentApi.latestContentFromLatestSnaps(capiClient, treatsSnapsRequest, adjustItemQuery)
        setOfContent = ContentApi.itemsFromSearchResponses(hydrateResponses)}
    yield Collection.treatContent(collection, setOfContent, snapContent)
  }

  def liveCollectionContentWithoutSnaps(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity)
                                (implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    val collectionWithoutSnaps = Collection.withoutSnaps(collection)
    for(setOfContent <- getLiveContentForCollection(collection, adjustSearchQuery))
      yield Collection.liveContent(collectionWithoutSnaps, setOfContent)
  }

  def liveCollectionContentWithSnaps(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity, adjustSnapItemQuery: AdjustItemQuery = identity)
                                   (implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    for {
      setOfContent <- getLiveContentForCollection(collection, adjustSearchQuery)
      snapContent <- getLiveLatestSnapContentForCollection(collection, adjustSnapItemQuery)
      linkSnapBrandingsByEdition <- getLiveLinkSnapBrandingsForCollection(collection)
    } yield Collection.liveContent(collection, setOfContent, snapContent, linkSnapBrandingsByEdition)
  }

  def draftCollectionContentWithoutSnaps(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity)
                                (implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    val collectionWithoutSnaps = Collection.withoutSnaps(collection)
    for(setOfContent <- getDraftContentForCollection(collection, adjustSearchQuery))
      yield Collection.draftContent(collectionWithoutSnaps, setOfContent)
  }

  def draftCollectionContentWithSnaps(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity, adjustSnapItemQuery: AdjustItemQuery = identity)
                                   (implicit capiClient: ContentApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {
    for {
      setOfContent <- getDraftContentForCollection(collection, adjustSearchQuery)
      snapContent <- getDraftLatestSnapContentForCollection(collection, adjustSnapItemQuery)
      linkSnapBrandingsByEdition <- getDraftLinkSnapBrandingsForCollection(collection)
    } yield Collection.draftContent(collection, setOfContent, snapContent, linkSnapBrandingsByEdition)
  }

  /**
    * Fetches content for the configured backfill query. The query can be manipulated for different
    * requirements by providing adjustment functions. The results then have their facia metadata
    * resolved using the collection information.
    */
  def backfillFromConfig(collectionConfig: CollectionConfig,
                         adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
                        (implicit capiClient: ContentApiClient, faciaClient: ApiClient, ec: ExecutionContext): Response[List[FaciaContent]] = {

    val backfillRequest = BackfillResolver.resolveFromConfig(collectionConfig)
    BackfillResolver.backfill(backfillRequest, adjustSearchQuery, adjustItemQuery)
  }

  private def updateContentProperties(content: FaciaContent, properties: ContentProperties): FaciaContent = content match {
    case curatedContent: CuratedContent => curatedContent.copy(properties = properties)
    case supportingCuratedContent: SupportingCuratedContent => supportingCuratedContent.copy(properties = properties)
    case snap: LatestSnap => snap.copy(properties = properties)
    case snap: LinkSnap => snap.copy(properties = properties)
  }

  private def applyDefaultBoost[T](
    content: T,
    default: BoostLevel,
    allowedBoosts: List[BoostLevel],
    getBoostLevel: T => BoostLevel,
    setBoostLevel: (T, BoostLevel) => T
  ): T =
    if (allowedBoosts.contains(getBoostLevel(content)))
      content
    else
      setBoostLevel(content, default)

  case class GroupBoostConfig(default: BoostLevel, allowedBoosts: List[BoostLevel], maxItems: Int)

  private val flexibleGeneralBoosts = List(
    // Splash
    GroupBoostConfig(BoostLevel.Default, List(BoostLevel.Default, BoostLevel.Boost, BoostLevel.MegaBoost, BoostLevel.GigaBoost), maxItems = 1),
    // Very large
    GroupBoostConfig(BoostLevel.MegaBoost, List(BoostLevel.MegaBoost), maxItems = 0),
    // Large
    GroupBoostConfig(BoostLevel.Boost, List(BoostLevel.Boost, BoostLevel.MegaBoost), maxItems = 0),
    // Standard
    GroupBoostConfig(BoostLevel.Default, List(BoostLevel.Default, BoostLevel.Boost, BoostLevel.MegaBoost), maxItems = 8)
  )

  private def boostsConfigFor(collectionType: String, groupsConfig: List[GroupConfig]): Option[List[GroupBoostConfig]] = {
    val groupsConfigBiggestFirst = groupsConfig.reverse
    condOpt(collectionType) {
      case "flexible/general" => flexibleGeneralBoosts.zip(groupsConfigBiggestFirst).map { case (boosts, groupConfig) =>
        boosts.copy(maxItems = groupConfig.maxItems.getOrElse(boosts.maxItems))
      }
    }
  }

  def applyDefaultBoostLevels(collectionConfig: CollectionConfig, contents: List[FaciaContent]): List[FaciaContent] = {
    applyDefaultBoostLevels[FaciaContent](collectionConfig.groupsConfig, collectionConfig.collectionType,
      contents,
      _.properties.boostLevel,
      (content, default) => updateContentProperties(content, content.properties.copy(boostLevel = default))
    )
  }

  // NB - this will not behave as expected if there are "gaps" in the curated content (ie there are no curated cards in the
  // second group but there are curated cards in the third group). But the fronts tool is supposed to prevent gaps.
  def applyDefaultBoostLevels[T](groupsConfig: Option[GroupsConfig], collectionType: String, contents: List[T], getBoostLevel: T => BoostLevel, setBoostLevel: (T, BoostLevel) => T): List[T] = {
    val contentsWithDefaultBoosts = for {
      gc <- groupsConfig
      boostsConfig <- boostsConfigFor(collectionType, gc.config)
    } yield {
      val (result, _) = boostsConfig.foldLeft((List.empty[T], contents)) { case ((processed, unprocessed), groupConfig) =>
        val (groupContents, remaining) = unprocessed.splitAt(groupConfig.maxItems)
        (processed ++ groupContents.map(applyDefaultBoost(_, groupConfig.default, groupConfig.allowedBoosts, getBoostLevel, setBoostLevel)), remaining)
      }
      result
    }

    contentsWithDefaultBoosts.getOrElse(contents)
  }
}
