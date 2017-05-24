package com.gu.facia.api.contentapi

import java.net.URI

import com.gu.contentapi.client.ContentApiClientLogic
import com.gu.contentapi.client.model.v1.{Content, ItemResponse, SearchResponse}
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import com.gu.facia.api.models.BrandingByEdition
import com.gu.facia.api.utils.ContentApiUtils._
import com.gu.facia.api.{CapiError, Response, UrlConstructError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

case class LatestSnapsRequest(snaps: Map[String, String]) {
  def join(other: LatestSnapsRequest): LatestSnapsRequest = this.copy(snaps = this.snaps ++ other.snaps)
}

case class LinkSnapsRequest(snaps: Map[String, String])

object ContentApi {
  type AdjustSearchQuery = SearchQuery => SearchQuery
  type AdjustItemQuery = ItemQuery => ItemQuery

  def buildHydrateQueries(client: ContentApiClientLogic, ids: List[String], adjustSearchQuery: AdjustSearchQuery = identity): Response[Seq[SearchQuery]] = {
    def queryForIds(ids: Seq[String]) = adjustSearchQuery(client.search
      .ids(ids mkString ",")
      .pageSize(ids.size)
      .showFields("internalPageCode"))

    Try(IdsSearchQueries.makeBatches(ids)) match {
        case Success(Some(batches)) =>
          Response.Right(batches.map(queryForIds))
        case _ =>
          Response.Left(UrlConstructError("Unable to construct url for ids search query (the constructed URL for a " +
            s"single ID must be too long!): ${ids.mkString(", ")}"))}
  }

  def getHydrateResponse(client: ContentApiClientLogic, searchQueries: Seq[SearchQuery])(implicit ec: ExecutionContext): Response[Seq[SearchResponse]] = {
    Response.Async.Right(Future.traverse(searchQueries)(client.getResponse)) mapError { err =>
      CapiError(s"Failed to hydrate content ${err.message}", err.cause)
    }
  }

  def itemsFromSearchResponses(searchResponses: Seq[SearchResponse]): Set[Content] =
    searchResponses.flatMap(_.results).toSet

  def buildBackfillQuery(apiQuery: String): Either[ItemQuery, SearchQuery] = {
    val uri = new URI(apiQuery.replaceAllLiterally("|", "%7C").replaceAllLiterally(" ", "%20"))
    val path = uri.getPath
    val rawParams = Option(uri.getQuery).map(parseQueryString).getOrElse(Nil).map {
      // wrap backfill tags in parentheses in case the editors wrote a raw OR query
      // makes it possible to safely append additional tags
      case (k, v) if k == "tag" => (k, s"($v)")
      case (k, v) if k == "show-fields" => (k, s"$v,internalPageCode")
      case param => param
    }
    val paramsWithFields =
      if (rawParams.exists {
        case ("show-fields", _) => true
        case _ => false
      }) rawParams else rawParams :+ ("show-fields" -> "internalPageCode")

    val paramsWithEditorsPicks = paramsWithFields :+ ("show-editors-picks" -> "false")

    if (path.startsWith("search")) {
      val searchQuery = SearchQuery()
      val queryWithParams = searchQuery.withParameters(paramsWithEditorsPicks.map { case (k, v) => k -> searchQuery.StringParameter(k, Some(v)) }.toMap)
      Right(queryWithParams)
    } else {
      val itemQuery = ItemQuery(path)
      val queryWithParams = itemQuery.withParameters(paramsWithEditorsPicks.map { case (k, v) => k -> itemQuery.StringParameter(k, Some(v)) }.toMap)
      Left(queryWithParams)
    }
  }

  def getBackfillResponse(client: ContentApiClientLogic, query: Either[ItemQuery, SearchQuery])
                         (implicit ec: ExecutionContext): Either[Response[ItemResponse], Response[SearchResponse]] = {
    query.right.map { itemQuery =>
      Response.Async.Right(client.getResponse(itemQuery)) mapError { err =>
        CapiError(s"Failed to get backfill response ${err.message}", err.cause)
      }
    }.left.map { searchQuery =>
      Response.Async.Right(client.getResponse(searchQuery)) mapError { err =>
        CapiError(s"Failed to get backfill response ${err.message}", err.cause)
      }
    }
  }

  def backfillContentFromResponse(response: Either[Response[ItemResponse], Response[SearchResponse]])
                                 (implicit ec: ExecutionContext): Response[List[Content]] = {
    response.fold(
      _.map { itemResponse =>
        (itemResponse.mostViewed.getOrElse(Nil) ++ itemResponse.results.getOrElse(Nil)).toList
      },
      _.map { searchResponse =>
        searchResponse.results.toList
      }
    )
  }

  def parseQueryString(queryString: String): Seq[(String, String)] = {
    val KeyValuePair = """([^=]+)=(.*)""".r

    queryString split "&" collect {
      case KeyValuePair(key, value) => (key, value)
    }
  }

  def latestContentFromLatestSnaps(capiClient: ContentApiClientLogic, latestSnapsRequest: LatestSnapsRequest, adjustItemQuery: AdjustItemQuery)
                                  (implicit ec: ExecutionContext): Response[Map[String, Option[Content]]] = {
    def itemQueryFromSnapUri(uri: String): ItemQuery =
      adjustItemQuery(capiClient.item(uri).pageSize(1).showFields("internalPageCode"))

    Response.Async.Right(
      Future.traverse(latestSnapsRequest.snaps) { case (id, uri) =>
        capiClient.getResponse(itemQueryFromSnapUri(uri))
          .map(_.results.getOrElse(Nil).headOption).map(id -> _)
      }.map(_.toMap))
  }

  def linkSnapBrandingsByEdition(capiClient: ContentApiClientLogic, linkSnapsRequest: LinkSnapsRequest)
    (implicit ec: ExecutionContext): Response[Map[String, BrandingByEdition]] = {

    def toIdAndUri(snap: (String, String)): (String, URI) = snap._1 -> new URI(snap._2)

    def itemQueryFromSnapUri(uri: URI): ItemQuery = capiClient.item(uri.getPath.stripPrefix("/")).pageSize(1)

    def isPossibleSectionFrontOrTagPage(snap: (String, URI)): Boolean = {
      val uri = snap._2
      !uri.isAbsolute && !uri.getPath.contains('+')
    }

    def brandingsFromResponse(response: ItemResponse): BrandingByEdition =
      response.section.map(_.brandingByEdition) orElse response.tag.map(_.brandingByEdition) getOrElse Map.empty

    Response.Async.Right(
      Future
      .traverse(linkSnapsRequest.snaps.map(toIdAndUri).filter(isPossibleSectionFrontOrTagPage)) { case (id, uri) =>
        capiClient.getResponse(itemQueryFromSnapUri(uri))
        .map(brandingsFromResponse).map(id -> _)
      }.map(_.toMap)
    )
  }
}
