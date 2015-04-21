package com.gu.facia.api.utils

import com.gu.contentapi.client.model.{Element, Tag, Content}
import com.gu.facia.api.models.{ImageReplace, ImageCutout, FaciaContent}
import org.joda.time.DateTime

import scala.util.Try

object FaciaContentImplicits {

  implicit class FaciaContentImplicit(fc: FaciaContent) {

    def maybeContent: Option[Content] = FaciaContentUtils.maybeContent(fc)
    def tags: List[com.gu.contentapi.client.model.Tag] = FaciaContentUtils.tags(fc)
    def webPublicationDateOption: Option[DateTime] = FaciaContentUtils.webPublicationDateOption(fc)
    def webPublicationDate: DateTime = FaciaContentUtils.webPublicationDate(fc)

    def id: String = FaciaContentUtils.id(fc)

    def embedType: Option[String] = FaciaContentUtils.embedType(fc)
    def embedCss: Option[String] = FaciaContentUtils.embedCss(fc)
    def embedUri: Option[String] = FaciaContentUtils.embedUri(fc)

    def itemKicker: Option[ItemKicker] = FaciaContentUtils.itemKicker(fc)

    def headlineOption: Option[String] = FaciaContentUtils.headlineOption(fc)
    def headline: String = FaciaContentUtils.headline(fc)

    def standfirst: Option[String] = FaciaContentUtils.standfirst(fc)

    def body(): Option[String] = FaciaContentUtils.body(fc)

    def webUrl: Option[String] = FaciaContentUtils.webUrl(fc)

    val DefaultSnapHref: String = "/"
    def href: Option[String] = FaciaContentUtils.href(fc)

    def mediaType: Option[MediaType] = FaciaContentUtils.mediaType(fc)

    def isLiveBlog: Boolean = FaciaContentUtils.isLiveBlog(fc)

    def isLive: Boolean = FaciaContentUtils.isLive(fc)

    def isPodcast: Boolean = FaciaContentUtils.isPodcast(fc)

    def isMedia: Boolean = FaciaContentUtils.isMedia(fc)

    def isEditorial: Boolean = FaciaContentUtils.isEditorial(fc)

    def isComment: Boolean = FaciaContentUtils.isComment(fc)

    def isAnalysis: Boolean = FaciaContentUtils.isAnalysis(fc)

    def isReview: Boolean = FaciaContentUtils.isReview(fc)

    def isLetters: Boolean = FaciaContentUtils.isLetters(fc)

    def isFeature: Boolean = FaciaContentUtils.isFeature(fc)

    def isCommentable = FaciaContentUtils.isCommentable(fc)
    def commentCloseDate = FaciaContentUtils.commentCloseDate(fc)

    def maybeShortUrl = FaciaContentUtils.maybeShortUrl(fc)
    def shortUrl: String = FaciaContentUtils.shortUrl(fc)
    def shortUrlPath = FaciaContentUtils.shortUrlPath(fc)
    def discussionId = FaciaContentUtils.discussionId(fc)
    def isBoosted: Boolean = FaciaContentUtils.isBoosted(fc)

    def showBoostedHeadline: Boolean = FaciaContentUtils.showBoostedHeadline(fc)

    def showQuotedHeadline: Boolean = FaciaContentUtils.showQuotedHeadline(fc)

    def showMainVideo: Boolean = FaciaContentUtils.showMainVideo(fc)

    def imageHide: Boolean = FaciaContentUtils.imageHide(fc)

    def sectionName: Option[String] = FaciaContentUtils.sectionName(fc)

    def maybeSection: Option[String] = FaciaContentUtils.maybeSection(fc)

    def section: String = FaciaContentUtils.section(fc)

    def byline: Option[String] = FaciaContentUtils.byline(fc)

    def showByline: Boolean = FaciaContentUtils.showByline(fc)

    def keywords: Seq[Tag] = FaciaContentUtils.keywords(fc)
    def nonKeywordTags: Seq[Tag] = FaciaContentUtils.nonKeywordTags(fc)
    def contributors: Seq[Tag] = FaciaContentUtils.contributors(fc)
    def isContributorPage: Boolean = FaciaContentUtils.isContributorPage(fc)
    def series: Seq[Tag] = FaciaContentUtils.series(fc)
    def blogs: Seq[Tag] = FaciaContentUtils.blogs(fc)
    def tones: Seq[Tag] = FaciaContentUtils.tones(fc)
    def types: Seq[Tag] = FaciaContentUtils.types(fc)
    def isVideo = FaciaContentUtils.isVideo(fc)
    def isGallery = FaciaContentUtils.isGallery(fc)
    def isAudio = FaciaContentUtils.isAudio(fc)
    def isCartoon = FaciaContentUtils.isCartoon(fc)
    def isCrossword = FaciaContentUtils.isCrossword(fc)

    def imageCutout: Option[ImageCutout] = FaciaContentUtils.imageCutout(fc)

    def supporting: List[FaciaContent] = FaciaContentUtils.supporting(fc)

    def starRating: Option[Int] = FaciaContentUtils.starRating(fc)

    def trailText: Option[String] = FaciaContentUtils.trailText(fc)

    def maybeWebTitle: Option[String] = FaciaContentUtils.maybeWebTitle(fc)

    def webTitle: String = FaciaContentUtils.webTitle(fc)

    def linkText = FaciaContentUtils.linkText(fc)

    def imageReplace: Option[ImageReplace] = FaciaContentUtils.imageReplace(fc)

    def elements: List[Element] = FaciaContentUtils.elements(fc)
    def cardStyle: CardStyle = FaciaContentUtils.cardStyle(fc)

    def isClosedForComments = FaciaContentUtils.isClosedForComments(fc)
  }
}
