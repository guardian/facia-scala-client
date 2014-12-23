package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.FreeHtmlKicker
import com.gu.facia.client.models.{TrailMetaData, Trail, CollectionJson}
import org.joda.time.DateTime
import org.scalatest.mock.MockitoSugar
import org.scalatest.{path, OneInstancePerTest, ShouldMatchers}
import org.scalatest.FreeSpec
import org.mockito.Mockito._


class CollectionTest extends FreeSpec with ShouldMatchers with MockitoSugar {
  "fromCollectionJson" - {
    val trailMetadata = mock[TrailMetaData]
    when(trailMetadata.headline).thenReturn(None)
    when(trailMetadata.href).thenReturn(None)
    when(trailMetadata.snapType).thenReturn(None)
    when(trailMetadata.snapCss).thenReturn(None)
    when(trailMetadata.snapUri).thenReturn(None)
    when(trailMetadata.trailText).thenReturn(None)
    when(trailMetadata.group).thenReturn(None)
    when(trailMetadata.imageSrc).thenReturn(None)
    when(trailMetadata.imageSrcWidth).thenReturn(None)
    when(trailMetadata.imageSrcHeight).thenReturn(None)
    when(trailMetadata.isBreaking).thenReturn(None)
    when(trailMetadata.isBoosted).thenReturn(None)
    when(trailMetadata.imageHide).thenReturn(None)
    when(trailMetadata.imageReplace).thenReturn(None)
    when(trailMetadata.showMainVideo).thenReturn(None)
    when(trailMetadata.showKickerTag).thenReturn(None)
    when(trailMetadata.showKickerSection).thenReturn(None)
    when(trailMetadata.byline).thenReturn(None)
    when(trailMetadata.showByline).thenReturn(None)
    when(trailMetadata.customKicker).thenReturn(None)
    when(trailMetadata.showKickerCustom).thenReturn(None)
    when(trailMetadata.imageCutoutReplace).thenReturn(None)
    when(trailMetadata.imageCutoutSrc).thenReturn(None)
    when(trailMetadata.imageCutoutSrcWidth).thenReturn(None)
    when(trailMetadata.imageCutoutSrcHeight).thenReturn(None)
    when(trailMetadata.showBoostedHeadline).thenReturn(None)
    when(trailMetadata.showQuotedHeadline).thenReturn(None)
    val trail = Trail("content-id", 1L, Some(trailMetadata))
    val collectionJson = CollectionJson(
      name = Some("name"),
      live = List(trail),
      draft = None,
      lastUpdated = new DateTime(1L),
      updatedBy = "test",
      updatedEmail = "test@example.com",
      displayName = Some("displayName"),
      href = Some("href")
    )
    val content = Content(
      "content-id", Some("section"), Some("Section Name"), None, "webTitle", "webUrl", "apiUrl",
      fields = Some(Map("headline" -> "Content headline", "href" -> "Content href", "trailText" -> "Content trailtext", "byline" -> "Content byline")),
      Nil, None, Nil, None
    )
    val contentMap = Map("content-id" -> content)

    "creates a Facia collection from the collection JSON" in {
      val collection = Collection.fromCollectionJsonAndContent(collectionJson, contentMap)
      collection should have (
        'name ("name"),
        'draft (None),
        'updatedBy ("test"),
        'updatedEmail ("test@example.com"),
        'displayName (Some("displayName")),
        'href (Some("href"))
      )
      collection.live.head should have (
        'content (content)
      )
    }

    "Uses content fields when no facia override exists" in {
      val collection = Collection.fromCollectionJsonAndContent(collectionJson, contentMap)
      collection.live.head should have (
        'headline ("Content headline"),
        'href ("Content href")
      )
    }

    "Resolves metadata from facia where fields exist" in {
      when(trailMetadata.headline).thenReturn(Some("trail headline"))
      when(trailMetadata.href).thenReturn(Some("trail href"))
      when(trailMetadata.customKicker).thenReturn(Some("Custom kicker"))
      when(trailMetadata.showKickerCustom).thenReturn(Some(true))

      val collection = Collection.fromCollectionJsonAndContent(collectionJson, contentMap)
      collection.live.head should have (
        'headline ("trail headline"),
        'href ("trail href"),
        'kicker (Some(FreeHtmlKicker("Custom kicker")))
      )
    }
  }
}
