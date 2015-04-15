package com.gu.facia.api.utils

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.utils.ContentApiUtils._
import com.gu.facia.client.models.MetaDataCommonFields

object CardStyle {

  def isExternalLink(content: Content, metaDataFields: MetaDataCommonFields): Boolean = (for {
    snapType <- metaDataFields.snapType
    href <- metaDataFields.href.orElse(content.safeFields.get("href"))
  } yield snapType == "link" && ExternalLinks.external(href)) getOrElse false

  def apply(content: Content, trailMetaData: MetaDataCommonFields): CardStyle = {
    if (isExternalLink(content, trailMetaData)) {
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
  override def toneString: String = "special-report"
}

case object LiveBlog extends CardStyle {
  override def toneString: String = "live"
}

case object DeadBlog extends CardStyle {
  override def toneString: String = "dead"
}

case object Feature extends CardStyle {
  override def toneString: String = "feature"
}

case object Editorial extends CardStyle {
  override def toneString: String = "editorial"
}

case object Comment extends CardStyle {
  override def toneString: String = "comment"
}

case object Podcast extends CardStyle {
  override def toneString: String = "podcast"
}

case object Media extends CardStyle {
  override def toneString: String = "media"
}

case object Analysis extends CardStyle {
  override def toneString: String = "analysis"
}

case object Review extends CardStyle {
  override def toneString: String = "review"
}

case object Letters extends CardStyle {
  override def toneString: String = "letters"
}

case object ExternalLink extends CardStyle {
  override def toneString: String = "external"
}

case object Default extends CardStyle {
  override def toneString: String = "news"
}
