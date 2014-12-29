package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.FreeHtmlKicker
import com.gu.facia.client.models.{CollectionConfigJson, CollectionJson, Trail, TrailMetaData}
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.{OneInstancePerTest, FreeSpec, ShouldMatchers}
import org.scalatest.mock.MockitoSugar


class CollectionTest extends FreeSpec with ShouldMatchers with MockitoSugar with OneInstancePerTest {
  "fromCollectionJson" - {
    val trailMetadata = spy(TrailMetaData.empty)

    val trail = Trail("content-id", 1L, Some(trailMetadata))
    val collectionJson = CollectionJson(
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
    val contentMap = Set(content)
    val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults())

    "creates a Facia collection from the collection JSON and provided config" in {
      val collection = Collection.fromCollectionJsonConfigAndContent(CollectionId("id"), collectionJson, collectionConfig, contentMap)
      collection should have (
        'id ("id"),
        'draft (None),
        'updatedBy ("test"),
        'updatedEmail ("test@example.com"),
        'displayName ("displayName"),
        'href (Some("href"))
      )
      collection.live.head should have (
        'content (content)
      )
    }

    "Uses content fields when no facia override exists" in {
      val collection = Collection.fromCollectionJsonConfigAndContent(CollectionId("id"), collectionJson, collectionConfig, contentMap)
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

      val collection = Collection.fromCollectionJsonConfigAndContent(CollectionId("id"), collectionJson, collectionConfig, contentMap)
      collection.live.head should have (
        'headline ("trail headline"),
        'href ("trail href"),
        'kicker (Some(FreeHtmlKicker("Custom kicker")))
      )
    }
  }
}
