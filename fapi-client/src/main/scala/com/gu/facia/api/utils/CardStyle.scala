package com.gu.facia.api.utils

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.ContentApiUtils._
import com.gu.facia.client.models.MetaDataCommonFields

object CardStyle {
  val specialReport = "special-report"
  val live = "live"
  val dead = "dead"
  val feature = "feature"
  val editorial = "editorial"
  val comment = "comment"
  val podcast = "podcast"
  val media = "media"
  val analysis = "analysis"
  val review = "review"
  val letters = "letters"
  val external = "external"
  val news = "news"

  def apply(content: Content, trailMetaData: MetaDataCommonFields): CardStyle = {
    val href = trailMetaData.href.orElse(content.safeFields.get("href"))
    if (trailMetaData.snapType == Some("link") && href.exists(ExternalLinks.external)) {
      ExternalLink
    } else if (content.tags.exists(_.id == "news/series/hsbc-files")) {
      SpecialReport
    } else if (content.isLiveBlog) {
      if (content.isLive) {
        LiveBlog
      } else {
        DeadBlog
      }
    } else if (content.isPodcast) {
      Podcast
    } else if (content.isMedia) {
      Media
    } else if (content.isEditorial) {
      Editorial
    } else if (content.isComment) {
      Comment
    } else if (content.isAnalysis) {
      Analysis
    } else if (content.isReview) {
      Review
    } else if (content.isLetters) {
      Letters
    } else if (content.isFeature) {
      Feature
    } else {
      Default
    }
  }
}

sealed trait CardStyle {
  def toneString: String
}

case object SpecialReport extends CardStyle {
  val toneString = CardStyle.specialReport
}

case object LiveBlog extends CardStyle {
  val toneString = CardStyle.live
}

case object DeadBlog extends CardStyle {
  val toneString = CardStyle.dead
}

case object Feature extends CardStyle {
  val toneString = CardStyle.feature
}

case object Editorial extends CardStyle {
  val toneString = CardStyle.editorial
}

case object Comment extends CardStyle {
  val toneString = CardStyle.comment
}

case object Podcast extends CardStyle {
  val toneString = CardStyle.podcast
}

case object Media extends CardStyle {
  val toneString = CardStyle.media
}

case object Analysis extends CardStyle {
  val toneString = CardStyle.analysis
}

case object Review extends CardStyle {
  val toneString = CardStyle.review
}

case object Letters extends CardStyle {
  val toneString = CardStyle.letters
}

case object ExternalLink extends CardStyle {
  val toneString = CardStyle.external
}

case object Default extends CardStyle {
  val toneString = CardStyle.news
}
