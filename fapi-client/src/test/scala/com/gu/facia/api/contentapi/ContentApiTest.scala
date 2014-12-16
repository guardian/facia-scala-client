package com.gu.facia.api.contentapi

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.{ItemResponse, Content, SearchResponse}
import com.gu.facia.api.Response
import lib.ExecutionContext
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{EitherValues, FreeSpec, OptionValues, ShouldMatchers}
import org.mockito.Mockito._


class ContentApiTest extends FreeSpec with ShouldMatchers with OptionValues with EitherValues with ScalaFutures with MockitoSugar with ExecutionContext {
  val testClient = new GuardianContentClient("test")

  "buildHydrateQuery" - {
    "should do a search query with the provided ids" in {
      ContentApi.buildHydrateQuery(testClient, List("1", "2")).parameters.get("ids").value should equal("1,2")
    }
  }

  "buildBackfillQuery" - {
    "when given a search backfill" - {
      val backfill = "search?tag=tone/analysis&section=world|us-news|australia-news"

      "should produce a searchQuery instance" in {
        ContentApi.buildBackfillQuery(testClient, backfill).isRight should equal(true)
      }

      "should wrap the tag parameter in brackets" in {
        val tagParam = ContentApi.buildBackfillQuery(testClient, backfill).right.value.parameters.get("tag").value
        tagParam should startWith("(")
        tagParam should endWith(")")
      }
    }

    "when given an item backfill" - {
      val backfill = "lifeandstyle/food-and-drink?show-most-viewed=true&show-editors-picks=false&hide-recent-content=true"

      "should produce an itemQuery instance" in {
        ContentApi.buildBackfillQuery(testClient, backfill).isLeft should equal(true)
      }

      "should use the given path for the itemQuery" in {
        ContentApi.buildBackfillQuery(testClient, backfill).left.value.id.value should equal("lifeandstyle/food-and-drink")
      }
    }
  }

  "backfillContentFromResponse" - {
    val contents = List(mock[Content], mock[Content])

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
      when(itemResponse.results) thenReturn contents
      val response: Either[Response[ItemResponse], Response[SearchResponse]] = Left(Response.Right(itemResponse))
      ContentApi.backfillContentFromResponse(response).asFuture.futureValue.fold(
        err => fail(s"expected contents result, got error $err"),
        result => result should be(contents)
      )
    }
  }
}
