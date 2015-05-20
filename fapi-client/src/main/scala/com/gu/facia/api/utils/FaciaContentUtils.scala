package com.gu.facia.api.utils

import com.gu.contentapi.client.model.{Element, Tag, Content}
import com.gu.facia.api.models._
import org.joda.time.DateTime

import scala.util.Try

object FaciaContentUtils {
  import ContentApiUtils._

  def fold[T](fc: FaciaContent)(c: (CuratedContent) => T, scc: (SupportingCuratedContent) => T,
    ls: (LinkSnap) => T, las: (LatestSnap) => T): T = fc match {
    case curatedContent: CuratedContent => c(curatedContent)
    case supportingCuratedContent: SupportingCuratedContent => scc(supportingCuratedContent)
    case linkSnap: LinkSnap => ls(linkSnap)
    case latestSnap: LatestSnap => las(latestSnap)}

  def maybeContent(fc: FaciaContent): Option[Content] = fold(fc)(
    curatedContent => Option(curatedContent.content),
    supportingCuratedContent => Option(supportingCuratedContent.content),
    linkSnap => None,
    latestSnap => latestSnap.latestContent)

  def tags(fc: FaciaContent): List[com.gu.contentapi.client.model.Tag] =
    maybeContent(fc).map(_.tags).getOrElse(Nil)

  def webPublicationDateOption(fc: FaciaContent): Option[DateTime] = fold(fc)(
    curatedContent => curatedContent.content.webPublicationDateOption,
    supportingCuratedContent => supportingCuratedContent.content.webPublicationDateOption,
    _ => None,
    latestSnap => latestSnap.latestContent.flatMap(_.webPublicationDateOption))

  def webPublicationDate(fc: FaciaContent): DateTime = webPublicationDateOption(fc).getOrElse(DateTime.now)

  def id(fc: FaciaContent): String = fold(fc)(
    curatedContent => curatedContent.content.id,
    supportingCuratedContent => supportingCuratedContent.content.id,
    linkSnap => linkSnap.id,
    latestSnap => latestSnap.id)

  def group(fc: FaciaContent): String = fold(fc)(
    curatedContent => curatedContent.group,
    supportingCuratedContent => supportingCuratedContent.group,
    linkSnap => linkSnap.group,
    latestSnap => latestSnap.group)

  def embedType(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.embedType,
    supportingCuratedContent => None,
    linkSnap => Option(linkSnap.snapType),
    latestSnap => Option("latest"))

  def embedCss(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.embedCss,
    supportingCuratedContent => None,
    linkSnap => linkSnap.snapCss,
    latestSnap => latestSnap.snapCss)

  def embedUri(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.embedUri,
    supportingCuratedContent => None,
    linkSnap => linkSnap.snapUri,
    latestSnap => latestSnap.snapUri)

  def itemKicker(fc: FaciaContent): Option[ItemKicker] =
    fold(fc)(
      curatedContent => curatedContent.kicker,
      supportingCuratedContent => supportingCuratedContent.kicker,
      linkSnap => linkSnap.kicker,
      latestSnap => latestSnap.kicker)

  def headlineOption(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => Option(curatedContent.headline),
    supportingCuratedContent => Option(supportingCuratedContent.headline),
    linkSnap => linkSnap.headline,
    latestSnap => latestSnap.headline.orElse(latestSnap.latestContent.map(_.fields.get("headline"))))

  def headline(fc: FaciaContent): String = headlineOption(fc).getOrElse("Missing Headline")

  def standfirst(fc: FaciaContent): Option[String] = fieldsGet(fc)(_.get("standfirst"))

  def body(fc: FaciaContent): Option[String] = fieldsGet(fc)(_.get("body"))

  def webUrl(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => Option(curatedContent.content.webUrl),
    supportingCuratedContent => Option(supportingCuratedContent.content.webUrl),
    linkSnap => linkSnap.snapUri,
    latestSnap => latestSnap.latestContent.map(_.webUrl))

  val DefaultSnapHref: String = "/"
  def href(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.href,
    supportingCuratedContent => supportingCuratedContent.href,
    linkSnap => linkSnap.href.orElse(linkSnap.snapUri),
    latestSnap => latestSnap.latestContent.map(_.id).orElse(latestSnap.snapUri))

  def mediaType(fc: FaciaContent): Option[MediaType] = {
    def mediaTypeFromContent(content: Content): Option[MediaType] =
      if (isGallery(fc)) Option(Gallery)
      else if (isAudio(fc)) Option(Audio)
      else if (isVideo(fc)) Option(Video)
      else None
    fold(fc)(
      curatedContent => mediaTypeFromContent(curatedContent.content),
      supportingCuratedContent => mediaTypeFromContent(supportingCuratedContent.content),
      linkSnap => None,
      latestSnap => latestSnap.latestContent.flatMap(mediaTypeFromContent))}

  def isLive(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.content.safeFields.get("liveBloggingNow").exists(_.toBoolean),
    supportingCuratedContent => supportingCuratedContent.content.safeFields.get("liveBloggingNow").exists(_.toBoolean),
    linkSnap => false,
    latestSnap => latestSnap.latestContent.exists(_.safeFields.get("liveBloggingNow").exists(_.toBoolean)))

  private def fieldsExists(fc: FaciaContent)(f: (Map[String, String]) => Boolean): Boolean = fold(fc)(
    curatedContent => f(curatedContent.content.safeFields),
    supportingCuratedContent => f(supportingCuratedContent.content.safeFields),
    _ => false,
    latestSnap => latestSnap.latestContent.exists(c => f(c.safeFields))
  )
  def isCommentable(fc: FaciaContent) = fieldsExists(fc)(_.get("commentable").exists(_.toBoolean))
  def commentCloseDate(fc: FaciaContent) = fieldsGet(fc)(_.get("commentCloseDate"))
  private def fieldsGet(fc: FaciaContent)(f: (Map[String, String]) => Option[String]): Option[String] = fold(fc)(
    curatedContent => f(curatedContent.content.safeFields),
    supportingCuratedContent => f(supportingCuratedContent.content.safeFields),
    linkSnap => None,
    latestSnap => latestSnap.latestContent.flatMap(c => f(c.safeFields))
  )
  def maybeShortUrl(fc: FaciaContent) = fieldsGet(fc)(_.get("shortUrl"))
  def shortUrl(fc: FaciaContent): String = maybeShortUrl(fc).getOrElse("")
  def shortUrlPath(fc: FaciaContent) = maybeShortUrl(fc).map(_.replace("http://gu.com", ""))
  def discussionId(fc: FaciaContent) = shortUrlPath(fc)

  def isBoosted(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.properties.isBoosted,
    supportingCuratedContent => supportingCuratedContent.properties.isBoosted,
    linkSnap => linkSnap.isBoosted,
    latestSnap => latestSnap.properties.isBoosted
  )
  def showBoostedHeadline(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.properties.showBoostedHeadline,
    supportingCuratedContent => supportingCuratedContent.properties.showBoostedHeadline,
    linkSnap => linkSnap.showBoostedHeadline,
    latestSnap => latestSnap.properties.showBoostedHeadline
  )
  def showQuotedHeadline(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.properties.showQuotedHeadline,
    supportingCuratedContent => supportingCuratedContent.properties.showQuotedHeadline,
    linkSnap => linkSnap.showQuotedHeadline,
    latestSnap => latestSnap.properties.showQuotedHeadline
  )
  def showMainVideo(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.properties.showMainVideo,
    supportingCuratedContent => supportingCuratedContent.properties.showMainVideo,
    linkSnap => linkSnap.showMainVideo,
    latestSnap => latestSnap.properties.showMainVideo
  )

  def sectionName(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.content.sectionName,
    supportingCuratedContent => supportingCuratedContent.content.sectionName,
    linkSnap => None,
    latestSnap => latestSnap.latestContent.flatMap(_.sectionName)
  )
  def maybeSection(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.content.sectionId,
    supportingCuratedContent => supportingCuratedContent.content.sectionId,
    linkSnap => None,
    latestSnap => latestSnap.latestContent.flatMap(_.sectionId)
  )
  def section(fc: FaciaContent): String = maybeSection(fc).getOrElse("")

  def byline(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.byline,
    supportingCuratedContent => supportingCuratedContent.byline,
    linkSnap => linkSnap.byline,
    latestSnap => latestSnap.latestContent.flatMap(_.safeFields.get("byline")))

  def showByline(fc: FaciaContent): Boolean = fold(fc)(
    curatedContent => curatedContent.properties.showByline,
    supportingCuratedContent => supportingCuratedContent.properties.showByline,
    linkSnap => linkSnap.showByLine,
    latestSnap => latestSnap.properties.showByline)

  private def tagsOfType(fc: FaciaContent)(tagType: String): Seq[Tag] = tags(fc).filter(_.`type` == tagType)
  def nonKeywordTags(fc: FaciaContent): Seq[Tag] = tags(fc).filterNot(_.`type` == "keyword")
  def keywords(fc: FaciaContent): Seq[Tag] = tagsOfType(fc)("keyword")
  def series(fc: FaciaContent): Seq[Tag] = tagsOfType(fc)("series")
  def blogs(fc: FaciaContent): Seq[Tag] = tagsOfType(fc)("blog")
  def tones(fc: FaciaContent): Seq[Tag] = tagsOfType(fc)("tone")
  def types(fc: FaciaContent): Seq[Tag] = tagsOfType(fc)("type")

  def contributors(fc: FaciaContent): Seq[Tag] = maybeContent(fc).map(_.contributors).getOrElse(Nil)
  def isContributorPage(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.contributors.nonEmpty)
  def isVideo(fc: FaciaContent) = maybeContent(fc).exists(_.isVideo)
  def isGallery(fc: FaciaContent) = maybeContent(fc).exists(_.isGallery)
  def isAudio(fc: FaciaContent) = maybeContent(fc).exists(_.isAudio)
  def isCartoon(fc: FaciaContent) = maybeContent(fc).exists(_.isCartoon)
  def isArticle(fc: FaciaContent) = maybeContent(fc).exists(_.isArticle)
  def isCrossword(fc: FaciaContent) = maybeContent(fc).exists(_.isCrossword)
  def isLiveBlog(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isLiveBlog)
  def isPodcast(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isPodcast)
  def isMedia(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isMedia)
  def isEditorial(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isEditorial)
  def isComment(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isComment)
  def isAnalysis(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isAnalysis)
  def isReview(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isReview)
  def isLetters(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isLetters)
  def isFeature(fc: FaciaContent): Boolean = maybeContent(fc).exists(_.isFeature)



  def supporting(fc: FaciaContent): List[FaciaContent] = fold(fc)(
    curatedContent => curatedContent.supportingContent,
    supportingCuratedContent => Nil,
    linkSnap => Nil,
    latestSnap => Nil)

  def starRating(fc: FaciaContent): Option[Int] = Try(fieldsGet(fc)(_.get("starRating")).map(_.toInt)).toOption.flatten

  def trailText(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => curatedContent.trailText,
    supportingCuratedContent => supportingCuratedContent.trailText,
    linkSnap => None,
    latestSnap => None)

  def  maybeWebTitle(fc: FaciaContent): Option[String] = fold(fc)(
    curatedContent => Option(curatedContent.content.webTitle),
    supportingCuratedContent => Option(supportingCuratedContent.content.webTitle),
    linkSnap => None,
    latestSnap => latestSnap.latestContent.map(_.webTitle))

  def webTitle(fc: FaciaContent): String = maybeWebTitle(fc).getOrElse("")

  def linkText(fc: FaciaContent) = maybeWebTitle(fc)

  def elements(fc: FaciaContent): List[Element] = fold(fc)(
    curatedContent => curatedContent.content.elements.getOrElse(Nil),
    supportingCuratedContent => supportingCuratedContent.content.elements.getOrElse(Nil),
    linkSnap => Nil,
    latestSnap => latestSnap.latestContent.flatMap(_.elements).getOrElse(Nil))

  def cardStyle(fc: FaciaContent): CardStyle = fold(fc)(
    curatedContent => curatedContent.cardStyle,
    supportingCuratedContent => supportingCuratedContent.cardStyle,
    linkSnap => if(linkSnap.href.exists(ExternalLinks.external)) ExternalLink else DefaultCardstyle,
    latestSnap => latestSnap.cardStyle)

  def image(fc: FaciaContent): Option[FaciaImage] = fold(fc)(
    curatedContent => curatedContent.image,
    supportingCuratedContent => supportingCuratedContent.image,
    linkSnap => linkSnap.image,
    latestSnap => latestSnap.image)

  def isClosedForComments (fc: FaciaContent) = fieldsExists(fc)(!_.get("commentCloseDate").exists(new DateTime(_).isAfterNow))

  def properties(fc: FaciaContent): Option[ContentProperties] = fold(fc)(
    curatedContent => Option(curatedContent.properties),
    supportingCuratedContent => Option(supportingCuratedContent.properties),
    linkSnap => None,
    latestSnap => Option(latestSnap.properties))
}
