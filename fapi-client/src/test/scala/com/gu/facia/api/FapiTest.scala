package com.gu.facia.api

import com.gu.contentapi.client.model.{SearchResponse, Content}
import com.gu.facia.client.models.{Trail, CollectionJson}
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}


class FapiTest extends FlatSpec with Matchers {

  private def makeContent(id: String) = Content(id, None, None, None, "", "", "", None, Nil, None)
  val contentOne: Content = makeContent("idone")
  val contentTwo: Content = makeContent("idtwo")
  val contentThree: Content = makeContent("idthree")

  private def makeSearchResponse(results: List[Content]): SearchResponse =
    SearchResponse("", "", 0, 0, 0, 0, 0, "", results)
  val emptySearchResponse: SearchResponse = makeSearchResponse(Nil)

  private def makeTrailWithId(id: String): Trail = Trail(id, 0L, None)
  val trailOne: Trail = makeTrailWithId("idone")
  val trailTwo: Trail = makeTrailWithId("idtwo")
  val trailThree: Trail = makeTrailWithId("idthree")

  private def makeCollectionWithTrails(trails: List[Trail]): CollectionJson =
    CollectionJson(None, trails, None, DateTime.now, "", "", None, None)
  val emptyCollection: CollectionJson = makeCollectionWithTrails(Nil)

  "Fapi Client" should "group things correctly" in {
    val searchResponse: SearchResponse = makeSearchResponse(List(contentOne, contentTwo, contentThree))
    val collection: CollectionJson = makeCollectionWithTrails(List(trailOne, trailTwo, trailThree))
    val content: List[FaciaContent] = FAPI.groupTrailAndContent(collection, searchResponse)
    content.length should be (3)
    for (faciaContent <- content)
      faciaContent.trail.id should be (faciaContent.content.id)
  }

  it should "drop trails that don't exist" in {
    val searchResponse: SearchResponse = makeSearchResponse(List(contentOne, contentTwo, contentThree))
    val collection: CollectionJson = makeCollectionWithTrails(List(trailOne, trailThree))
    val content: List[FaciaContent] = FAPI.groupTrailAndContent(collection, searchResponse)
    content.length should be (2)
    for (faciaContent <- content)
      faciaContent.trail.id should be (faciaContent.content.id)
  }

  it should "drop content that doesn't exist" in {
    val searchResponse: SearchResponse = makeSearchResponse(List(contentOne))
    val collection: CollectionJson = makeCollectionWithTrails(List(trailOne, trailTwo, trailThree))
    val content: List[FaciaContent] = FAPI.groupTrailAndContent(collection, searchResponse)
    content.length should be (1)
    for (faciaContent <- content)
      faciaContent.trail.id should be (faciaContent.content.id)
  }


}
