package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.{ContentProperties, DefaultCardstyle, FaciaContentUtils}
import com.gu.facia.client.models.{TrailMetaData, Trail}
import org.scalatest.{FreeSpec, Matchers}

class FaciaContentHelperTest extends FreeSpec with Matchers {

  val emptyTrail: Trail = Trail("no-id", 0, Option(TrailMetaData.empty))

  val emptyContentProperties =
    ContentProperties(
      isBreaking = false,
      isBoosted = false,
      imageHide = false,
      showBoostedHeadline = false,
      showMainVideo = false,
      showKickerTag = false,
      showByline = false,
      showQuotedHeadline = false,
      imageSlideshowReplace = false)

  "should return 'Missing Headline' when the headline is None in a Snaps" in {
    val snap = LatestSnap("myId", None, DefaultCardstyle, None, None, None, None, None, None, "myGroup", None, emptyContentProperties, None, None)
    FaciaContentUtils.headlineOption(snap) should equal(None)
  }

  "should return the headline for a CuratedContent" in {
    val content = Content("myId", None, None, None, "myTitle", "myUrl", "myApi", Some(Map("byline" -> "myByline")), Nil, None, Nil, None)
    val cc = CuratedContent(content, None, Nil, DefaultCardstyle, "The headline", None, None, "myGroup", None, emptyContentProperties, None, None, None, None, None)
    FaciaContentUtils.headlineOption(cc) should equal(Some("The headline"))
  }

  "should return 'Missing href' when the href is None in a CuratedContent" in {
    val content = Content("myId", None, None, None, "myTitle", "myUrl", "myApi", Some(Map("byline" -> "myByline")), Nil, None, Nil, None)
    val cc = CuratedContent(content, None, Nil, DefaultCardstyle, "The headline", None, None, "myGroup", None, emptyContentProperties, None, None, None, None, None)
    FaciaContentUtils.href(cc) should equal(None)
  }

  "should return a href for a LatestSnap" in {
    val snap = LatestSnap("myId", None, DefaultCardstyle, None, None, None, None, Some("The href"), None, "myGroup", None, emptyContentProperties, None, None)
    FaciaContentUtils.href(snap) should equal(Some("The href"))
  }

  "should return a byline for a LatestSnap" in {
    val content = Content("myId", None, None, None, "myTitle", "myUrl", "myApi", Some(Map("byline" -> "myByline")), Nil, None, Nil, None)
    val snap = LatestSnap.fromTrailAndContent(emptyTrail, Option(content))
    FaciaContentUtils.byline(snap) should equal(Some("myByline"))
  }

}
