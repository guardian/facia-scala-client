package com.gu.facia.api.models

import com.gu.contentapi.client.model.v1.{Content, ContentFields, ContentType}
import com.gu.facia.api.utils.ContentApiUtils._
import com.gu.facia.api.utils._
import com.gu.facia.client.models.{Branded, CollectionConfigJson, CollectionJson, Trail, TrailMetaData}
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsArray, JsString, Json}

class CollectionTest extends AnyFreeSpec with Matchers with MockitoSugar with OneInstancePerTest {
  val trailMetadata = spy(TrailMetaData.empty)
  val trail = Trail("internal-code/page/123", 1, None, Some(trailMetadata))
  val collectionJson = CollectionJson(
    live = List(trail),
    draft = None,
    treats = None,
    lastUpdated = new DateTime(1),
    updatedBy = "test",
    updatedEmail = "test@example.com",
    displayName = Some("displayName"),
    href = Some("collectionHref"),
    previously = None,
    targetedTerritory = None
  )
  val content = Content(
    id = "content-id",
    `type` = ContentType.Article,
    sectionId = Some("section-id"),
    sectionName = Some("Section Name"),
    webPublicationDate = None,
    webTitle = "webTitle",
    webUrl = "webUrl",
    apiUrl = "apiUrl",
    fields = Some(ContentFields(
      headline = Some("Content headline"),
      trailText = Some("Content trailtext"),
      byline = Some("Content byline"),
      internalPageCode = Some(123)
    )),
    tags = Nil,
    elements = None,
    references = Nil,
    isExpired = None,
    blocks = None,
    rights = None,
    crossword = None
  )

  val contents = Set(content)
  val collectionConfig = CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults(
    metadata = Some(List(Branded)),
    href = Some("collectionConfigHref")
  ))

  def makeLatestSnap(
    id: String = "id",
    maybeFrontPublicationDate: Option[Long] = None,
    snapUri: Option[String] = None,
    snapCss: Option[String] = None,
    latestContent: Option[Content] = None,
    headline: Option[String] = None,
    href: Option[String] = None,
    trailText: Option[String] = None,
    group: String = "0",
    image: Option[FaciaImage] = None,
    contentProperties: ContentProperties = ContentProperties.fromResolvedMetaData(ResolvedMetaData.Default),
    byline: Option[String] = None,
    kicker: Option[ItemKicker] = None): LatestSnap =
    LatestSnap(
      id,
      maybeFrontPublicationDate,
      DefaultCardstyle,
      ContentFormat.defaultContentFormat,
      snapUri,
      snapCss,
      latestContent,
      headline,
      href,
      trailText,
      group,
      image,
      contentProperties,
      byline,
      kicker,
      latestContent map (_.brandingByEdition) getOrElse Map.empty
    )

  def makeLinkSnap(
    id: String = "id",
    maybeFrontPublicationDate: Option[Long] = None,
    snapType: String = Snap.DefaultType,
    snapUri: Option[String] = None,
    snapCss: Option[String] = None,
    atomId: Option[String] = None,
    headline: Option[String] = None,
    href: Option[String] = None,
    trailText: Option[String] = None,
    group: String = "0",
    image: Option[FaciaImage] = None,
    contentProperties: ContentProperties = ContentProperties.fromResolvedMetaData(ResolvedMetaData.Default),
    byline: Option[String] = None,
    kicker: Option[ItemKicker] = None): LinkSnap =
    LinkSnap(
      id,
      maybeFrontPublicationDate,
      snapType,
      snapUri,
      snapCss,
      atomId,
      headline,
      href,
      trailText,
      group,
      image,
      contentProperties,
      byline,
      kicker,
      Map.empty
    )


  "fromCollectionJson" - {

    val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)

    "creates a Facia collection from the collection JSON and provided config" in {
      collection should have(
        Symbol("id")("id"),
        Symbol("draft")(None),
        Symbol("href")(Some("collectionConfigHref")),
        Symbol("lastUpdated")(Some(new DateTime(1))),
        Symbol("updatedBy")(Some("test")),
        Symbol("updatedEmail")(Some("test@example.com")),
        Symbol("displayName")("displayName")
      )

      collection.collectionConfig should have (
        Symbol("href")(Some("collectionConfigHref"))
      )
    }

    "creates metadata from provided config JSON" in {
      collection.collectionConfig should have(
        Symbol("metadata") (Some(List(Branded)))
      )
    }
  }

  "liveContent" - {
    val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJson), collectionConfig)

    "Uses content fields when no facia override exists" in {
      val curatedContent = Collection.liveContent(collection, contents)
      curatedContent.head should have (
        Symbol("headline") ("Content headline")
      )
    }

    "Resolves metadata from facia where fields exist" in {
      when(trailMetadata.headline).thenReturn(Some("trail headline"))
      when(trailMetadata.href).thenReturn(Some("trail href"))
      when(trailMetadata.customKicker).thenReturn(Some("Custom kicker"))
      when(trailMetadata.showKickerCustom).thenReturn(Some(true))

      val curatedContent = Collection.liveContent(collection, contents)
      curatedContent.head should have (
        Symbol("headline") ("trail headline"),
        Symbol("href") (Some("trail href")),
        Symbol("kicker") (Some(FreeHtmlKicker("Custom kicker")))
      )
    }

    "excludes trails where no corresponding content is found" in {
      val trail2 = Trail("content-id-2", 2, None, Some(trailMetadata))
      val collectionJson = CollectionJson(
        live = List(trail, trail2),
        draft = None,
        treats = None,
        lastUpdated = new DateTime(1),
        updatedBy = "test",
        updatedEmail = "test@example.com",
        displayName = Some("displayName"),
        href = Some("href"),
        previously = None,
        targetedTerritory = None
      )
      val curatedContent = Collection.liveContent(collection, contents)
      curatedContent.flatMap{
        case c: CuratedContent => Some(c)
        case _ => None
      }.map(_.content.id) should equal(List("content-id"))
    }

    "Successfully retrieve snaps from snapContent for latest snaps" in {
      val snapOne = Trail("snap/1415985080061", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("link"), "snapUri" -> JsString("abc")))))
      val snapTwo = Trail("snap/5345345215342", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("link"), "snapCss" -> JsString("css")))))
      val snapLatestOne = Trail("snap/8474745745660", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("latest"), "href" -> JsString("uk")))))
      val snapLatestTwo = Trail("snap/4324234234234", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("latest"), "href" -> JsString("culture")))))

      val snapContentOne = Content(
        id = "content-id-one",
        `type` = ContentType.Article,
        sectionId = Some("section-id"),
        sectionName = Some("Section Name"),
        webPublicationDate = None,
        webTitle = "webTitle",
        webUrl = "webUrl",
        apiUrl = "apiUrl",
        fields = Some(ContentFields(headline = Some("Content headline"), trailText = Some("Content trailtext"), byline = Some("Content byline"))),
        tags = Nil,
        elements = None,
        references = Nil,
        isExpired = None,
        blocks = None,
        rights = None,
        crossword = None
      )

      val snapContentTwo = Content(
        id = "content-id-two",
        `type` = ContentType.Article,
        sectionId = Some("section-id"),
        sectionName = Some("Section Name"),
        webPublicationDate = None,
        webTitle = "webTitle",
        webUrl = "webUrl",
        apiUrl = "apiUrl",
        fields = Some(ContentFields(headline = Some("Content headline"), trailText = Some("Content trailtext"), byline = Some("Content byline"))),
        tags = Nil,
        elements = None,
        references = Nil,
        isExpired = None,
        blocks = None,
        rights = None,
        crossword = None
      )

      val snapContent = Map("snap/8474745745660" -> Some(snapContentOne), "snap/4324234234234" -> Some(snapContentTwo))

      val collectionJsonTwo = collectionJson.copy(live = List(snapOne, snapTwo, snapLatestOne, snapLatestTwo))

      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJsonTwo), collectionConfig)

      val faciaContent = Collection.liveContent(collection, Set.empty, snapContent)

      faciaContent.length should be (4)
      faciaContent(0) should be (makeLinkSnap(id = "snap/1415985080061", maybeFrontPublicationDate = Option(1L), snapUri = Some("abc")))
      faciaContent(1) should be (makeLinkSnap(id = "snap/5345345215342", maybeFrontPublicationDate = Option(1L), snapCss = Some("css")))
      faciaContent(2) should be (makeLatestSnap(
                                  id = "snap/8474745745660",
                                  maybeFrontPublicationDate = Option(1L),
                                  href = Some("uk"),
                                  headline = Some("Content headline"),
                                  byline = Some("Content byline"),
                                  trailText = Some("Content trailtext"),
                                  latestContent = Some(snapContentOne)))
      faciaContent(3) should be (makeLatestSnap(
                                  id = "snap/4324234234234",
                                  maybeFrontPublicationDate = Option(1L),
                                  href = Some("culture"),
                                  headline = Some("Content headline"),
                                  byline = Some("Content byline"),
                                  trailText = Some("Content trailtext"),
                                  latestContent = Some(snapContentTwo)))
    }
  }

  "liveIdsWithoutSnaps" - {
    val trailOne = Trail("internal-code/page/1", 1, None, Some(trailMetadata))
    val trailTwo = Trail("internal-code/page/2", 1, None, Some(trailMetadata))
    val trailThree = Trail("artanddesign/gallery/2014/feb/28/beyond-basquiat-black-artists", 1, None, Some(trailMetadata))
    val snapOne = Trail("snap/1415985080061", 1, None, Some(trailMetadata))
    val snapTwo = Trail("snap/5345345215342", 1, None, Some(trailMetadata))

    val collectionJsonTwo = collectionJson.copy(live = List(trailOne, snapOne, snapTwo, trailTwo, trailThree))

    val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJsonTwo), collectionConfig)

    "Collection should start with 5 items" in {
      collection.live.length should be (5)
    }

    "Removes snaps from a collection and returns the ids to query" in {
      Collection.liveIdsWithoutSnaps(collection) should be
        List("internal-code/page/1",
          "internal-code/page/2",
          "artanddesign/gallery/2014/feb/28/beyond-basquiat-black-artists")
    }

    "Empty out snaps" in {
      val collectionJsonAllSnaps = collectionJson.copy(live = List(snapOne, snapTwo))
      val collectionAllSnaps = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJsonAllSnaps), collectionConfig)

      Collection.liveIdsWithoutSnaps(collectionAllSnaps) should be (List.empty)
    }
  }

  "Collection" - {
    "filter out the snaps in live" in {
        val trailOne = Trail("internal-code/page/1", 1, None, Some(trailMetadata))
        val trailTwo = Trail("internal-code/page/2", 1, None, Some(trailMetadata))
        val snapOne = Trail("snap/1415985080061", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("link"), "snapUri" -> JsString("abc")))))
        val snapTwo = Trail("snap/5345345215342", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("link"), "snapCss" -> JsString("css")))))
        val snapLatestOne = Trail("snap/8474745745660", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("latest"), "href" -> JsString("uk")))))
        val snapLatestTwo = Trail("snap/4324234234234", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("latest"), "href" -> JsString("culture")))))

        val snapContentOne = Content(
          id = "content-id-one",
          `type` = ContentType.Article,
          sectionId = Some("section-id"),
          sectionName = Some("Section Name"),
          webPublicationDate = None,
          webTitle = "webTitle",
          webUrl = "webUrl",
          apiUrl = "apiUrl",
          fields = Some(ContentFields(headline = Some("Content headline"), trailText = Some("Content trailtext"), byline = Some("Content byline"))),
          tags = Nil,
          elements = None,
          references = Nil,
          isExpired = None,
          blocks = None,
          rights = None,
          crossword = None
        )

        val snapContentTwo = Content(
          id = "content-id-two",
          `type` = ContentType.Article,
          sectionId = Some("section-id"),
          sectionName = Some("Section Name"),
          webPublicationDate = None,
          webTitle = "webTitle",
          webUrl = "webUrl",
          apiUrl = "apiUrl",
          fields = Some(ContentFields(headline = Some("Content headline"), trailText = Some("Content trailtext"), byline = Some("Content byline"))),
          tags = Nil,
          elements = None,
          references = Nil,
          isExpired = None,
          blocks = None,
          rights = None,
          crossword = None
        )

        val snapContent = Map("snap/8474745745660" -> Some(snapContentOne), "snap/4324234234234" -> Some(snapContentTwo))

        val collectionJsonTwo = collectionJson.copy(live = List(snapOne, snapTwo, trailOne, snapLatestOne, snapLatestTwo, trailTwo))

        val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJsonTwo), collectionConfig)

        val collectionWithoutSnaps = Collection.withoutSnaps(collection)

        collectionWithoutSnaps.live.length should be (2)
        collectionWithoutSnaps.live(0).id should be ("internal-code/page/1")
        collectionWithoutSnaps.live(1).id should be ("internal-code/page/2")
      }

    "filter out the snaps in draft" in {
      val trailOne = Trail("internal-code/page/1", 1, None, Some(trailMetadata))
      val trailTwo = Trail("internal-code/page/2", 1, None, Some(trailMetadata))
      val snapOne = Trail("snap/1415985080061", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("link"), "snapUri" -> JsString("abc")))))
      val snapTwo = Trail("snap/5345345215342", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("link"), "snapCss" -> JsString("css")))))
      val snapLatestOne = Trail("snap/8474745745660", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("latest"), "href" -> JsString("uk")))))
      val snapLatestTwo = Trail("snap/4324234234234", 1, None, Some(TrailMetaData(Map("snapType" -> JsString("latest"), "href" -> JsString("culture")))))

      val snapContentOne = Content(
        id = "content-id-one",
        `type` = ContentType.Article,
        sectionId = Some("section-id"),
        sectionName = Some("Section Name"),
        webPublicationDate = None,
        webTitle = "webTitle",
        webUrl = "webUrl",
        apiUrl = "apiUrl",
        fields = Some(ContentFields(headline = Some("Content headline"), trailText = Some("Content trailtext"), byline = Some("Content byline"))),
        tags = Nil,
        elements = None,
        references = Nil,
        isExpired = None,
        blocks = None,
        rights = None,
        crossword = None
      )

      val snapContentTwo = Content(
        id = "content-id-two",
        `type` = ContentType.Article,
        sectionId = Some("section-id"),
        sectionName = Some("Section Name"),
        webPublicationDate = None,
        webTitle = "webTitle",
        webUrl = "webUrl",
        apiUrl = "apiUrl",
        fields = Some(ContentFields(headline = Some("Content headline"), trailText = Some("Content trailtext"), byline = Some("Content byline"))),
        tags = Nil,
        elements = None,
        references = Nil,
        isExpired = None,
        blocks = None,
        rights = None,
        crossword = None
      )

      val snapContent = Map("snap/8474745745660" -> Some(snapContentOne), "snap/4324234234234" -> Some(snapContentTwo))

      val collectionJsonTwo = collectionJson.copy(draft = Option(List(snapOne, snapTwo, trailTwo, snapLatestOne, snapLatestTwo, trailOne)))

      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJsonTwo), collectionConfig)

      val collectionWithoutSnaps = Collection.withoutSnaps(collection)

      collectionWithoutSnaps.draft.map(_.length) should be (Some(2))
      collectionWithoutSnaps.draft.map(_.apply(0).id) should be (Some("internal-code/page/2"))
      collectionWithoutSnaps.draft.map(_.apply(1).id) should be (Some("internal-code/page/1"))
    }
  }

  "Internal code" - {
    val baseContent = Content(
      id = "content-id-one",
      `type` = ContentType.Article,
      sectionId = Some("section-id"),
      sectionName = Some("Section Name"),
      webPublicationDate = None,
      webTitle = "webTitle",
      webUrl = "webUrl",
      apiUrl = "apiUrl",
      fields = None,
      tags = Nil,
      elements = None,
      references = Nil,
      isExpired = None,
      blocks = None,
      rights = None,
      crossword = None
    )

    val contents = Set(
      baseContent.copy(fields = Some(ContentFields(internalPageCode = Some(3), headline = Some("red apple"), trailText = Some("Content trailtext"), byline = Some("Content byline")))),
      baseContent.copy(fields = Some(ContentFields(internalPageCode = Some(2), headline = Some("straight banana"), trailText = Some("Content trailtext"), byline = Some("Content byline")))),
      baseContent.copy(fields = Some(ContentFields(internalPageCode = Some(6), headline = Some("hairy kiwi"), trailText = Some("Content trailtext"), byline = Some("Content byline")))),
      baseContent.copy(fields = Some(ContentFields(internalPageCode = Some(7), headline = Some("straw berry"), trailText = Some("Content trailtext"), byline = Some("Content byline"))))
    )

    def makeTrail(id: String) =
      Trail(id, 0, None, None)
    def makeTrailWithSupporting(id: String, supporting: Trail*) =
      Trail(id, 0, None, Some(TrailMetaData(Map("supporting" -> JsArray(supporting.map(Json.toJson(_)))))))
    "trails with internalPageCode" in {
      val trail = Trail("internal-code/page/2", 1, None, Some(trailMetadata))
      val collectionJsonWithPageCode = collectionJson.copy(live = List(trail))
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJsonWithPageCode), collectionConfig)
      val result = Collection.liveContent(collection, contents)
      result.length should be (1)
      result.head should have (
        Symbol("headline")("straight banana")
      )
    }
    "supporting with internalPageCode" in {
      val supporting = makeTrail("internal-code/page/2")
      val trail = makeTrailWithSupporting("internal-code/page/7", supporting)
      val collectionJsonWithPageCode = collectionJson.copy(live = List(trail))
      val collection = Collection.fromCollectionJsonConfigAndContent("id", Some(collectionJsonWithPageCode), collectionConfig)
      val result = Collection.liveContent(collection, contents)
      result.length should be (1)
      result.head should have (
        Symbol("headline")("straw berry")
      )
      FaciaContentUtils.headline(FaciaContentUtils.supporting(result.head).head) should be ("straight banana")
    }
  }
}
