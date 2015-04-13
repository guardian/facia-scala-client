package com.gu.facia.api.contentapi

import java.net.URI

import com.gu.contentapi.client.{GuardianContentClient, ContentApiClientLogic}
import com.gu.contentapi.client.model._
import com.gu.facia.api.{UrlConstructError, CapiError, Response}

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Try}

case class LatestSnapsRequest(snaps: Map[String, String]) {
  def join(other: LatestSnapsRequest): LatestSnapsRequest = this.copy(snaps = this.snaps ++ other.snaps)
}

object ContentApi {
  type AdjustSearchQuery = SearchQuery => SearchQuery
  type AdjustItemQuery = ItemQuery => ItemQuery

  def buildHydrateQueries(client: ContentApiClientLogic, ids: List[String], adjustSearchQuery: AdjustSearchQuery = identity): Response[Seq[SearchQuery]] = {
    def queryForIds(ids: Seq[String]) = adjustSearchQuery(client.search
      .ids(ids mkString ",")
      .pageSize(ids.size)
      .showFields("internalContentCode"))

    Try(IdsSearchQueries.makeBatches(ids)(ids => client.getUrl(queryForIds(ids)))) match {
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
      // ensure internalContentCode is present on queries
      case (k, v) if k == "show-fields" => (k, s"$v,internalContentCode")
      case param => param
    }
    val paramsWithFields =
      if (rawParams.exists {
        case ("show-fields", _) => true
        case _ => false
      }) rawParams else rawParams :+ ("show-fields" -> "internalContentCode")
    val paramsWithEditorsPicks =
      if (paramsWithFields.exists {
        case ("show-editors-picks", _) => true
        case _ => false
      }) paramsWithFields else paramsWithFields :+ ("show-editors-picks" -> "true")

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
          itemResponse.editorsPicks ++ itemResponse.mostViewed ++ itemResponse.results
        },
      _.map { searchResponse =>
          searchResponse.results
        }
    )
  }

  def parseQueryString(queryString: String): Seq[(String, String)] = {
    val KeyValuePair = """([^=]+)=(.*)""".r

    queryString split "&" collect {
      case KeyValuePair(key, value) => (key, value)
    }
  }

  def latestContentFromLatestSnaps(capiClient: GuardianContentClient, latestSnapsRequest: LatestSnapsRequest, adjustItemQuery: AdjustItemQuery)
                                  (implicit ec: ExecutionContext): Response[Map[String, Option[Content]]] = {
    def itemQueryFromSnapUri(uri: String): ItemQuery =
      adjustItemQuery(capiClient.item(uri).pageSize(1).showFields("internalContentCode"))

    Response.Async.Right(
      Future.traverse(latestSnapsRequest.snaps) { case (id, uri) =>
        capiClient.getResponse(itemQueryFromSnapUri(uri))
          .map(_.results.headOption).map(id -> _)
      }.map(_.toMap))
  }
}
