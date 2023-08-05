package com.gu.facia.api.models

import com.gu.contentapi.client.model.v1.ContentFields
import com.gu.facia.api.utils.{ContentProperties, DefaultCardstyle, FaciaContentUtils}
import com.gu.facia.client.models.{Trail, TrailMetaData}
import lib.TestContent
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class FaciaContentHelperTest extends AnyFreeSpec with Matchers with TestContent {

  val emptyTrail: Trail = Trail("no-id", 0, None, Option(TrailMetaData.empty))

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
      showLivePlayable = false,
      imageSlideshowReplace = false)

  "should return 'Missing Headline' when the headline is None in a Snaps" in {
    val snap = LatestSnap("myId",
      None,
      DefaultCardstyle,
      ContentFormat.defaultContentFormat,
      None,
      None,
      None,
      None,
      None,
      None,
      "myGroup",
      None,
      emptyContentProperties,
      None,
      None,
      Map.empty
    )
    FaciaContentUtils.headlineOption(snap) should equal(None)
  }

  "should return the headline for a CuratedContent" in {
    val content = baseContent.copy(fields = Some(ContentFields(headline = Some("myTitle"), trailText = Some("Content trailtext"), byline = Some("Content byline"))))
    val cc = CuratedContent(content, None, Nil, DefaultCardstyle, ContentFormat.defaultContentFormat, "The headline", None, None, "myGroup", None, emptyContentProperties, None, None, None, None, None, Map.empty)
    FaciaContentUtils.headlineOption(cc) should equal(Some("The headline"))
  }

  "should return 'Missing href' when the href is None in a CuratedContent" in {
    val content = baseContent.copy(fields = Some(ContentFields(headline = Some("myTitle"), trailText = Some("Content trailtext"), byline = Some("Content byline"))))
    val cc = CuratedContent(content, None, Nil, DefaultCardstyle, ContentFormat.defaultContentFormat, "The headline", None, None, "myGroup", None, emptyContentProperties, None, None, None, None, None, Map.empty)
    FaciaContentUtils.href(cc) should equal(None)
  }

  "should return a href for a LatestSnap" in {
    val snap = LatestSnap("myId",
      None,
      DefaultCardstyle,
      ContentFormat.defaultContentFormat,
      None,
      None,
      None,
      None,
      Some("The href"),
      None,
      "myGroup",
      None,
      emptyContentProperties,
      None,
      None,
      Map.empty
    )
    FaciaContentUtils.href(snap) should equal(Some("The href"))
  }

  "should return a byline for a LatestSnap" in {
    val content = baseContent.copy(fields = Some(ContentFields(headline = Some("myTitle"), trailText = Some("Content trailtext"), byline = Some("myByline"))))
    val snap = LatestSnap.fromTrailAndContent(emptyTrail, Option(content))
    FaciaContentUtils.byline(snap) should equal(Some("myByline"))
  }

  "should return an atomId for a Link Snap that is given the capi url for an interactive atom" in {
    val linkSnap = LinkSnap(
      "myLinkId",
      None,
      "interactive",
      Some("https://content.guardianapis.com/atom/interactive/interactives/2017/06/general-election"),
      None,
      Some("atomId123"),
      Some("Good headline"),
      None,
      Some("Trail text"),
      "myGroup",
      None,
      emptyContentProperties,
      None,
      None,
      Map.empty
    )

    val content = baseContent.copy(fields = Some(ContentFields(headline = Some("myTitle"), trailText = Some("Content trailtext"), byline = Some("myByline"))))
    val snap = LatestSnap.fromTrailAndContent(emptyTrail, Option(content))
    FaciaContentUtils.atomId(linkSnap) should equal(Some("atomId123"))
  }

}
