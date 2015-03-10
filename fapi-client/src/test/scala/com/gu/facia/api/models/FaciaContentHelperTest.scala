package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import org.scalatest.{FreeSpec, Matchers}

class FaciaContentHelperTest extends FreeSpec with Matchers {

  "should return 'Missing Headline' when the headline is None in a Snaps" in {
    val snap = LatestSnap("myId", None, None, None, None, None, None, "myGroup", None, false, false, false, false, false, false, None, false, None, None, false, false)
    FaciaContent.headline(snap) should equal("Missing Headline")
  }

  "should return the headline for a CuratedContent" in {
    val content = Content("myId", None, None, None, "myTitle", "myUrl", "myApi", Some(Map("byline" -> "myByline")), Nil, None, Nil, None)
    val cc = CuratedContent(content, Nil, "The headline", None, None, "myGroup", None, false, false, false, false, false, None, false, None, None, false, false)
    FaciaContent.headline(cc) should equal("The headline")
  }

  "should return 'Missing href' when the href is None in a CuratedContent" in {
    val content = Content("myId", None, None, None, "myTitle", "myUrl", "myApi", Some(Map("byline" -> "myByline")), Nil, None, Nil, None)
    val cc = CuratedContent(content, Nil, "The headline", None, None, "myGroup", None, false, false, false, false, false, None, false, None, None, false, false)
    FaciaContent.href(cc) should equal("Missing href")
  }

  "should return a href for a LatestSnap" in {
    val snap = LatestSnap("myId", None, None, None, None, Some("The href"), None, "myGroup", None, false, false, false, false, false, false, None, false, None, None, false, false)
    FaciaContent.href(snap) should equal("myId")
  }

  "should return a byline for a LatestSnap" in {
    val content = Content("myId", None, None, None, "myTitle", "myUrl", "myApi", Some(Map("byline" -> "myByline")), Nil, None, Nil, None)
    val snap = LatestSnap("myId", None, None, Some(content), None, Some("The href"), None, "myGroup", None, false, false, false, false, false, false, None, false, None, None, false, false)
    FaciaContent.byline(snap) should equal(Some("myByline"))
  }

}
