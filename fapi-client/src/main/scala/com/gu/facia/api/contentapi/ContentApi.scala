package com.gu.facia.api.contentapi

import java.net.URI

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model._
import com.gu.facia.api.{CapiError, Response}

import scala.concurrent.ExecutionContext


object ContentApi {
  type AdjustSearchQuery = SearchQuery => SearchQuery
  type AdjustItemQuery = ItemQuery => ItemQuery

  def buildHydrateQuery(client: GuardianContentClient, ids: List[String]): SearchQuery = {
    client.search
      .ids(ids mkString ",")
  }

  def getHydrateResponse(client: GuardianContentClient, searchQuery: SearchQuery)(implicit ec: ExecutionContext): Response[SearchResponse] = {
    Response.Async.Right(client.getResponse(searchQuery)) recover { err =>
      CapiError(s"Failed to hydrate content ${err.message}", err.cause)
    }
  }

  def itemsFromSearchResponse(searchResponse: SearchResponse): Set[Content] = {
    searchResponse.results.toSet
  }

  def buildBackfillQuery(client: GuardianContentClient, apiQuery: String): Either[ItemQuery, SearchQuery] = {
    val uri = new URI(apiQuery.replaceAllLiterally("|", "%7C").replaceAllLiterally(" ", "%20"))
    val path = uri.getPath
    val params = Option(uri.getQuery).map(parseQueryString).getOrElse(Nil).map {
      // wrap backfill tags in parentheses in case the editors wrote a raw OR query
      // makes it possible to safely append additional tags
      case (k, v) if k == "tag" => (k, s"($v)")
      case param => param
    }

    if (path.startsWith("search")) {
      val searchQuery = SearchQuery()
      val queryWithParams = searchQuery.withParameters(params.map { case (k, v) => k -> searchQuery.StringParameter(k, Some(v)) }.toMap)
      Right(queryWithParams)
    } else {
      val itemQuery = ItemQuery(Some(path))
      val queryWithParams = itemQuery.withParameters(params.map { case (k, v) => k -> itemQuery.StringParameter(k, Some(v)) }.toMap)
      Left(itemQuery)
    }
  }

  def getBackfillResponse(client: GuardianContentClient, query: Either[ItemQuery, SearchQuery])
                         (implicit ec: ExecutionContext): Either[Response[ItemResponse], Response[SearchResponse]] = {
    query.right.map { itemQuery =>
      Response.Async.Right(client.getResponse(itemQuery)) recover { err =>
        CapiError(s"Failed to get backfill response ${err.message}", err.cause)
      }
    }.left.map { searchQuery =>
      Response.Async.Right(client.getResponse(searchQuery)) recover { err =>
        CapiError(s"Failed to get backfill response ${err.message}", err.cause)
      }
    }
  }

  def backfillContentFromResponse(response: Either[Response[ItemResponse], Response[SearchResponse]])
                                 (implicit ec: ExecutionContext): Response[List[Content]] = {
    response.fold(
      _.map(_.results),
      _.map(_.results)
    )
  }

  def parseQueryString(queryString: String): Seq[(String, String)] = {
    val KeyValuePair = """([^=]+)=(.*)""".r

    queryString split "&" collect {
      case KeyValuePair(key, value) => (key, value)
    }
  }
}
