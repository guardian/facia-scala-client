package com.gu.facia.api.utils

import com.gu.contentapi.client.model.{Content, Tag}
import com.gu.facia.api.FaciaContent
import com.gu.facia.client.models.{CollectionConfig, Trail}

object ItemKicker {
  private def firstTag(item: Content): Option[Tag] = item.tags.headOption

  def fromContentAndTrail(content: Content, trail: Trail, config: Option[CollectionConfig]): Option[ItemKicker] = {
    lazy val maybeTag = firstTag(content)

    def tagKicker = maybeTag.map(TagKicker.fromTag)

    def sectionKicker = for {
      name <- content.sectionName
      id <- content.sectionId
    } yield SectionKicker(name.capitalize, "/" + id)

    trail.meta.flatMap(_.customKicker) match {
      case Some(kicker)
        if trail.meta.flatMap(_.snapType).exists(_ == "latest") &&
          trail.meta.flatMap(_.showKickerCustom).exists(identity) &&
          trail.meta.flatMap(_.snapUri).isDefined => Some(FreeHtmlKickerWithLink(kicker, s"/${trail.meta.flatMap(_.snapUri).get}"))
      case Some(kicker) if trail.meta.flatMap(_.showKickerCustom).exists(identity) => Some(FreeHtmlKicker(kicker))
      case _ => if (trail.meta.flatMap(_.showKickerTag).exists(identity) && maybeTag.isDefined) {
        tagKicker
      } else if (trail.meta.flatMap(_.showKickerSection).exists(identity)) {
        sectionKicker
      } else if (config.exists(_.showTags.exists(identity)) && maybeTag.isDefined) {
        tagKicker
      } else if (config.exists(_.showSections.exists(identity))) {
        sectionKicker
      } else if (!config.exists(_.hideKickers.exists(identity))) {
        tonalKicker(content, trail)
      } else {
        None
      }
    }
  }

  def fromContentAndTrail(faciaContent: FaciaContent): Option[ItemKicker] = {
    import faciaContent._
    lazy val maybeTag = firstTag(content)

    def tagKicker = maybeTag.map(TagKicker.fromTag)

    def sectionKicker = for {
      name <- content.sectionName
      id <- content.sectionId
    } yield SectionKicker(name.capitalize, "/" + id)

    trail.meta.flatMap(_.customKicker) match {
      case Some(kicker)
        if trail.meta.flatMap(_.snapType).exists(_ == "latest") &&
          trail.meta.flatMap(_.showKickerCustom).exists(identity) &&
          trail.meta.flatMap(_.snapUri).isDefined => Some(FreeHtmlKickerWithLink(kicker, s"/${trail.meta.flatMap(_.snapUri).get}"))
      case Some(kicker) if trail.meta.flatMap(_.showKickerCustom).exists(identity) => Some(FreeHtmlKicker(kicker))
      case _ => if (trail.meta.flatMap(_.showKickerTag).exists(identity) && maybeTag.isDefined) {
        tagKicker
      } else if (trail.meta.flatMap(_.showKickerSection).exists(identity)) {
        sectionKicker
      } else if (config.exists(_.showTags.exists(identity)) && maybeTag.isDefined) {
        tagKicker
      } else if (config.exists(_.showSections.exists(identity))) {
        sectionKicker
      } else if (!config.exists(_.hideKickers.exists(identity))) {
        tonalKicker(content, trail)
      } else {
        None
      }
    }
  }

  private def tonalKicker(content: Content, trail: Trail): Option[ItemKicker] = {
    def tagsOfType(tagType: String): Seq[Tag] = content.tags.filter(_.`type` == tagType)
    val types: Seq[Tag] = tagsOfType("type")
    val tones: Seq[Tag] = tagsOfType("tone")

    lazy val isReview = tones.exists(t => Tags.reviewMappings.contains(t.id))
    lazy val isAnalysis = tones.exists(_.id == Tags.Analysis)
    lazy val isPodcast = types.exists(_.id == Tags.Podcast) || content.tags.exists(_.podcast.isDefined)
    lazy val isCartoon = types.exists(_.id == Tags.Cartoon)

    if (trail.meta.flatMap(_.isBreaking).exists(identity)) {
      Some(BreakingNewsKicker)
    } else if (content.safeFields.get("liveBloggingNow").exists(_.toBoolean)) {
      Some(LiveKicker)
    } else if (isPodcast) {
      val series = content.tags.find(_.`type` == "series") map { seriesTag =>
        Series(seriesTag.webTitle, seriesTag.webUrl)
      }
      Some(PodcastKicker(series))
    } else if (isAnalysis) {
      Some(AnalysisKicker)
    } else if (isReview) {
      Some(ReviewKicker)
    } else if (isCartoon) {
      Some(CartoonKicker)
    } else {
      None
    }
  }

  val TagsThatDoNotAutoKicker = Set(
    "commentisfree/commentisfree"
  )

  def seriesOrBlogKicker(item: Content) =
    item.tags.find({ tag =>
      Set("series", "blog").contains(tag.`type`) && !TagsThatDoNotAutoKicker.contains(tag.id)
    }).map(TagKicker.fromTag)

  /** Used for de-duping bylines */
  def kickerText(itemKicker: ItemKicker): Option[String] = itemKicker match {
    case PodcastKicker(Some(series)) => Some(series.name)
    case TagKicker(name, _, _) => Some(name)
    case SectionKicker(name, _) => Some(name)
    case FreeHtmlKicker(body) => Some(body)
    case FreeHtmlKickerWithLink(body, _) => Some(body)
    case _ => None
  }
}

case class Series(name: String, url: String)

sealed trait ItemKicker

case object BreakingNewsKicker extends ItemKicker
case object LiveKicker extends ItemKicker
case object AnalysisKicker extends ItemKicker
case object ReviewKicker extends ItemKicker
case object CartoonKicker extends ItemKicker
case class PodcastKicker(series: Option[Series]) extends ItemKicker

object TagKicker {
  def fromTag(tag: Tag) = TagKicker(tag.webTitle, tag.webUrl, tag.id)
}

case class TagKicker(name: String, url: String, id: String) extends ItemKicker

case class SectionKicker(name: String, url: String) extends ItemKicker
case class FreeHtmlKicker(body: String) extends ItemKicker
case class FreeHtmlKickerWithLink(body: String, url: String) extends ItemKicker


object Tags {
  val Analysis = "tone/analysis"
  val Crossword = "type/crossword"
  val Podcast = "type/podcast"
  val Editorial = "tone/editorials"
  val Cartoon = "type/cartoon"
  val Letters = "tone/letters"

  object VisualTone {
    val Live = "live"
    val Comment = "comment"
    val Feature = "feature"
    val News = "news"
  }

  val liveMappings = Seq(
    "tone/minutebyminute"
  )

  val commentMappings = Seq (
    "tone/comment"
  )

  val mediaTypes = Seq(
    "type/video",
    "type/audio",
    "type/gallery",
    "type/picture"
  )

  val featureMappings = Seq(
    "tone/features",
    "tone/recipes",
    "tone/interview",
    "tone/performances",
    "tone/extract",
    "tone/reviews",
    "tone/albumreview",
    "tone/livereview",
    "tone/childrens-user-reviews"
  )

  val reviewMappings = Seq(
    "tone/reviews"
  )
}