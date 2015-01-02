package com.gu.facia.api.contentapi

import java.net.URI

import com.gu.contentapi.client.ContentApiClientLogic
import com.gu.contentapi.client.model._
import com.gu.facia.api.{CapiError, Response}

import scala.concurrent.{Future, ExecutionContext}
import scala.util.Try

object ContentApi {
  type AdjustSearchQuery = SearchQuery => SearchQuery
  type AdjustItemQuery = ItemQuery => ItemQuery

  def buildHydrateQueries(client: ContentApiClientLogic, ids: List[String], adjustSearchQuery: AdjustSearchQuery = identity): Try[Seq[SearchQuery]] = {
    def queryForIds(ids: Seq[String]) = adjustSearchQuery(client.search
      .ids(ids mkString ",")
      .pageSize(ids.size)
      .showFields("internalContentCode"))

    Try {
      IdsSearchQueries.makeBatches(ids)(ids => client.getUrl(queryForIds(ids)).get) match {
        case Some(batches) =>
          batches.map(queryForIds)

        case None =>
          throw new RuntimeException("Unable to construct url for ids search query (the constructed URL for a " +
            s"single ID must be too long!): ${ids.mkString(", ")}")
      }
    }
  }

  def getHydrateResponse(client: ContentApiClientLogic, searchQueries: Seq[SearchQuery])(implicit ec: ExecutionContext): Response[Seq[SearchResponse]] = {
    Response.Async.Right(Future.traverse(searchQueries)(client.getResponse)) recover { err =>
      CapiError(s"Failed to hydrate content ${err.message}", err.cause)
    }
  }

  def itemsFromSearchResponses(searchResponses: Seq[SearchResponse]): Set[Content] =
    searchResponses.flatMap(_.results).toSet

  def buildBackfillQuery(client: ContentApiClientLogic, apiQuery: String): Either[ItemQuery, SearchQuery] = {
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
    val params =
      if (rawParams.exists {
        case ("show-fields", _) => true
        case _ => false
      }) rawParams else rawParams :+ ("show-fields" -> "internalContentCode")

    if (path.startsWith("search")) {
      val searchQuery = SearchQuery()
      val queryWithParams = searchQuery.withParameters(params.map { case (k, v) => k -> searchQuery.StringParameter(k, Some(v)) }.toMap)
      Right(queryWithParams)
    } else {
      val itemQuery = ItemQuery(Some(path))
      val queryWithParams = itemQuery.withParameters(params.map { case (k, v) => k -> itemQuery.StringParameter(k, Some(v)) }.toMap)
      Left(queryWithParams)
    }
  }

  def getBackfillResponse(client: ContentApiClientLogic, query: Either[ItemQuery, SearchQuery])
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
