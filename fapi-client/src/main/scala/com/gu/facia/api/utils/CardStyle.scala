package com.gu.facia.api.utils

import com.gu.contentapi.client.model.v1.Content
import com.gu.facia.api.utils.ContentApiUtils._
import com.gu.facia.client.models.{TrailMetaData, MetaDataCommonFields}
import java.security.MessageDigest
import java.math.BigInteger

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

  private val salt = "a-public-salt3W#ywHav!p+?r+W2$E6="
  private val digest = MessageDigest.getInstance("MD5")

  private def md5(input: String): Option[String] = {
    try {
      digest.update(input.getBytes(), 0, input.length)

      Option(new BigInteger(1, digest.digest()).toString(16))
    } catch {
      case _: Throwable => None
    }
  }

  def apply(content: Content, trailMetaData: MetaDataCommonFields): CardStyle = {
    val href = trailMetaData.href

    val hashedTagIds: Seq[String] = content.tags.flatMap { id =>
      md5(salt + id)
    }

    if (trailMetaData.snapType == Some("link") && href.exists(ExternalLinks.external)) {
      ExternalLink
    } else if (hashedTagIds.contains("344ce3383665e23496bedd160675780c") // news/series/hsbc-files
      || hashedTagIds.contains("d36fa10d66bf5ff85894829d3829d9e1")
      || hashedTagIds.contains("ae4bd9f302c420d242a8da91a47a9ddd")
      || hashedTagIds.contains("9d89e70b7d99e776ffb741c0b9ab8854")      // us-news/series/counted-us-police-killings
      || hashedTagIds.contains("7037b49de72275eb72b73a111da31849")      // australia-news/series/healthcare-in-detention
      || hashedTagIds.contains("efb4e63b9a3a926314724b45764a5a5a") ) {  // society/series/this-is-the-nhs
      SpecialReport
    } else if (content.isLiveBlog) {
      if (content.isLive) {
        LiveBlog
      } else {
        DeadBlog
      }
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
      DefaultCardstyle
    }
  }

  def fromContent(content: Content) = apply(content, TrailMetaData.empty)
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

case object DefaultCardstyle extends CardStyle {
  val toneString = CardStyle.news
}
