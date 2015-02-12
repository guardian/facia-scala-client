package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.FreeHtmlKicker
import com.gu.facia.client.models.{CollectionConfigJson, CollectionJson, Trail, TrailMetaData}
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.{OneInstancePerTest, FreeSpec, ShouldMatchers}
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsString


class CollectionTest extends FreeSpec with ShouldMatchers with MockitoSugar with OneInstancePerTest {
  val trailMetadata = spy(TrailMetaData.empty)
  val trail = Trail("internal-code/content/CODE", 1, Some(trailMetadata))
  val collectionJson = CollectionJson(
    live = List(trail),
    draft = None,
    treats = None,
    lastUpdated = new DateTime(1),
    updatedBy = "test",
    updatedEmail = "test@example.com",
    displayName = Some("displayName"),
    href = Some("collectionHref"),
    None
  )
  val content = Content(
    "content-id", Some("section"), Some("Section Name"), None, "webTitle", "webUrl", "apiUrl",
    fields = Some(Map("internalContentCode" -> "CODE", "headline" -> "Content headline", "href" -> "Content href", "trailText" -> "Content trailtext", "byline" -> "Content byline")),
    Nil, None, Nil, None
  )
  val contents = Set(content)
  val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults(href = Some("collectionConfigHref")))


  "fromCollectionJson" - {
    "creates a Facia collection from the collection JSON and provided config" in {
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)
      collection should have(
        'id("id"),
        'draft(None),
        'lastUpdated(Some(new DateTime(1))),
        'updatedBy(Some("test")),
        'updatedEmail(Some("test@example.com")),
        'displayName("displayName")
      )

      collection.collectionConfig should have (
        'href(Some("collectionConfigHref"))
      )
    }
  }

  "liveContent" - {
    val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)

    "Uses content fields when no facia override exists" in {
      val curatedContent = Collection.liveContent(collection, contents)
      curatedContent.head should have (
        'headline ("Content headline"),
        'href (Some("Content href"))
      )
    }

    "Resolves metadata from facia where fields exist" in {
      when(trailMetadata.headline).thenReturn(Some("trail headline"))
      when(trailMetadata.href).thenReturn(Some("trail href"))
      when(trailMetadata.customKicker).thenReturn(Some("Custom kicker"))
      when(trailMetadata.showKickerCustom).thenReturn(Some(true))

      val curatedContent = Collection.liveContent(collection, contents)
      curatedContent.head should have (
        'headline ("trail headline"),
        'href (Some("trail href")),
        'kicker (Some(FreeHtmlKicker("Custom kicker")))
      )
    }

    "excludes trails where no corresponding content is found" in {
      val trail2 = Trail("content-id-2", 2, Some(trailMetadata))
      val collectionJson = CollectionJson(
        live = List(trail, trail2),
        draft = None,
        treats = None,
        lastUpdated = new DateTime(1),
        updatedBy = "test",
        updatedEmail = "test@example.com",
        displayName = Some("displayName"),
        href = Some("href"),
        None
      )
      val curatedContent = Collection.liveContent(collection, contents)
      curatedContent.flatMap{
        case c: CuratedContent => Some(c)
        case _ => None
      }.map(_.content.id) should equal(List("content-id"))
    }

    "Successfully retrieve snaps from snapContent for latest snaps" in {
      val snapOne = Trail("snap/1415985080061", 1, Some(TrailMetaData(Map("snapType" -> JsString("link"), "snapUri" -> JsString("abc")))))
      val snapTwo = Trail("snap/5345345215342", 1, Some(TrailMetaData(Map("snapType" -> JsString("link"), "snapCss" -> JsString("css")))))
      val snapLatestOne = Trail("snap/8474745745660", 1, Some(TrailMetaData(Map("snapType" -> JsString("latest"), "href" -> JsString("uk")))))
      val snapLatestTwo = Trail("snap/4324234234234", 1, Some(TrailMetaData(Map("snapType" -> JsString("latest"), "href" -> JsString("culture")))))

      val snapContentOne = Content(
        "content-id-one", Some("section"), Some("Section Name"), None, "webTitle", "webUrl", "apiUrl",
        fields = Some(Map("internalContentCode" -> "CODE", "headline" -> "Content headline", "href" -> "Content href", "trailText" -> "Content trailtext", "byline" -> "Content byline")),
        Nil, None, Nil, None)
      val snapContentTwo = Content(
        "content-id-two", Some("section"), Some("Section Name"), None, "webTitle", "webUrl", "apiUrl",
        fields = Some(Map("internalContentCode" -> "CODE", "headline" -> "Content headline", "href" -> "Content href", "trailText" -> "Content trailtext", "byline" -> "Content byline")),
        Nil, None, Nil, None)

      val snapContent = Map("snap/8474745745660" -> Some(snapContentOne), "snap/4324234234234" -> Some(snapContentTwo))

      val collectionJsonTwo = collectionJson.copy(live = List(snapOne, snapTwo, snapLatestOne, snapLatestTwo))

      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJsonTwo), collectionConfig)

      val faciaContent = Collection.liveContent(collection, Set.empty, snapContent)

      faciaContent.length should be (4)
      faciaContent(0) should be (LinkSnap("snap/1415985080061", Some("abc"), None))
      faciaContent(1) should be (LinkSnap("snap/5345345215342", None, Some("css")))
      faciaContent(2) should be (LatestSnap("snap/8474745745660", None, None, Some(snapContentOne)))
      faciaContent(3) should be (LatestSnap("snap/4324234234234", None, None, Some(snapContentTwo)))
    }
  }

  "liveIdsWithoutSnaps" - {
    val trailOne = Trail("internal-code/content/1", 1, Some(trailMetadata))
    val trailTwo = Trail("internal-code/content/2", 1, Some(trailMetadata))
    val trailThree = Trail("artanddesign/gallery/2014/feb/28/beyond-basquiat-black-artists", 1, Some(trailMetadata))
    val snapOne = Trail("snap/1415985080061", 1, Some(trailMetadata))
    val snapTwo = Trail("snap/5345345215342", 1, Some(trailMetadata))

    val collectionJsonTwo = collectionJson.copy(live = List(trailOne, snapOne, snapTwo, trailTwo, trailThree))

    val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJsonTwo), collectionConfig)

    "Collection should start with 5 items" in {
      collection.live.length should be (5)
    }

    "Removes snaps from a collection and returns the ids to query" in {
      Collection.liveIdsWithoutSnaps(collection) should be
        List("internal-code/content/1",
          "internal-code/content/2",
          "artanddesign/gallery/2014/feb/28/beyond-basquiat-black-artists")
    }

    "Empty out snaps" in {
      val collectionJsonAllSnaps = collectionJson.copy(live = List(snapOne, snapTwo))
      val collectionAllSnaps = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJsonAllSnaps), collectionConfig)

      Collection.liveIdsWithoutSnaps(collectionAllSnaps) should be (List.empty)
    }
  }

  "importance" - {
    "Resolve critical correctly" in {
      val collectionConfigWithImportance = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults(importance = Option("critical")))

      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfigWithImportance)

      collection.collectionConfig.importance should be (Critical)
    }
  }
}
