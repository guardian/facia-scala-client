package com.gu.facia.api.contentapi

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.v1.{ItemResponse, SearchResponse, Content}
import com.gu.facia.api.Response
import lib.ExecutionContext
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest._
import org.mockito.Mockito._


class ContentApiTest extends FreeSpec
  with ShouldMatchers
  with OptionValues
  with EitherValues
  with TryValues
  with ScalaFutures
  with MockitoSugar
  with ExecutionContext {
  val testClient = new GuardianContentClient("test")

  "buildHydrateQueries" - {
    "should do a search query with the provided ids" in {
      ContentApi.buildHydrateQueries(testClient, List("1", "2")).asFuture.futureValue.fold(
        err => fail(s"expected hydrated queries, got error $err"),
        result => result.head.parameters.get("ids").value should equal("1,2")
      )
    }

    "sets page size to the number of curated items" in {
      ContentApi.buildHydrateQueries(testClient, List("1", "2")).asFuture.futureValue.fold(
        err => fail(s"expected hydrated queries, got error $err"),
        result => result.head.parameters.get("page-size").value should equal("2")
      )
    }
  }

  "buildBackfillQuery" - {
    "when given a search backfill" - {
      val backfill = "search?tag=tone/analysis&section=world|us-news|australia-news"

      "should produce a searchQuery instance" in {
        ContentApi.buildBackfillQuery(backfill).isRight should equal(true)
      }

      "should wrap the tag parameter in brackets" in {
        val tagParam = ContentApi.buildBackfillQuery(backfill).right.value.parameters.get("tag").value
        tagParam should startWith("(")
        tagParam should endWith(")")
      }

      "adds the internalPageCode field" in {
        ContentApi.buildBackfillQuery(backfill).right.value.parameters.get("show-fields").value should equal ("internalPageCode")
      }

      "preserves existing show-fields when adding internaPageCode" in {
        val backfillWithFields = s"$backfill&show-fields=headline"
        ContentApi.buildBackfillQuery(backfillWithFields).right.value.parameters.get("show-fields").value should equal ("headline,internalPageCode")
      }
    }

    "when given an item backfill" - {
      val backfill = "lifeandstyle/food-and-drink?show-most-viewed=true&show-editors-picks=false&hide-recent-content=true"

      "should produce an itemQuery instance" in {
        ContentApi.buildBackfillQuery(backfill).isLeft should equal(true)
      }

      "should use the given path for the itemQuery" in {
        ContentApi.buildBackfillQuery(backfill).left.value.id should equal("lifeandstyle/food-and-drink")
      }

      "adds the internalPageCode field" in {
        ContentApi.buildBackfillQuery(backfill).left.value.parameters.get("show-fields").value should equal ("internalPageCode")
      }

      "preserves existing show-fields when adding internalPageCode" in {
        val backfillWithFields = s"$backfill&show-fields=headline"
        ContentApi.buildBackfillQuery(backfillWithFields).left.value.parameters.get("show-fields").value should equal ("headline,internalPageCode")
      }

      "will add editors picks false if they aren't explicitly on the query" in {
        val backfill = "lifeandstyle/food-and-drink?show-most-viewed=true&hide-recent-content=true"
        ContentApi.buildBackfillQuery(backfill).left.value.parameters.get("show-editors-picks").value should equal ("false")
      }

      "will force editors picks to false if they are explicitly exluded on the query" in {
        val backfill = "lifeandstyle/food-and-drink?show-most-viewed=true&show-editors-picks=false&hide-recent-content=true"
        ContentApi.buildBackfillQuery(backfill).left.value.parameters.get("show-editors-picks").value should equal ("false")
      }

      "will force editors picks to false if they are explicitly included on the query" in {
        val backfill = "lifeandstyle/food-and-drink?show-most-viewed=true&show-editors-picks=true&hide-recent-content=true"
        ContentApi.buildBackfillQuery(backfill).left.value.parameters.get("show-editors-picks").value should equal ("false")
      }
    }
  }

  "backfillContentFromResponse" - {
    val contents = List(mock[Content], mock[Content])
    val someContents = Some(contents)

    "will extract backfill content from search response" in {
      val searchResponse = mock[SearchResponse]
      when(searchResponse.results) thenReturn contents
      val response: Either[Response[ItemResponse], Response[SearchResponse]] = Right(Response.Right(searchResponse))
      ContentApi.backfillContentFromResponse(response).asFuture.futureValue.fold(
        err => fail(s"expected contents result, got error $err"),
        result => result should be(contents)
      )
    }

    "will extract backfill items from item response" in {
      val itemResponse = mock[ItemResponse]
      when(itemResponse.results) thenReturn someContents
      when(itemResponse.editorsPicks) thenReturn Some(Nil)
      when(itemResponse.mostViewed) thenReturn Some(Nil)
      val response: Either[Response[ItemResponse], Response[SearchResponse]] = Left(Response.Right(itemResponse))
      ContentApi.backfillContentFromResponse(response).asFuture.futureValue.fold(
        err => fail(s"expected contents result, got error $err"),
        result => result should be(contents)
      )
    }

    "includes most viewed in item query backfill" in {
      val itemResponse = mock[ItemResponse]
      when(itemResponse.results) thenReturn Some(Nil)
      when(itemResponse.editorsPicks) thenReturn Some(Nil)
      when(itemResponse.mostViewed) thenReturn someContents
      val response: Either[Response[ItemResponse], Response[SearchResponse]] = Left(Response.Right(itemResponse))
      ContentApi.backfillContentFromResponse(response).asFuture.futureValue.fold(
        err => fail(s"expected contents result, got error $err"),
        result => result should be(contents)
      )
    }
  }
}
