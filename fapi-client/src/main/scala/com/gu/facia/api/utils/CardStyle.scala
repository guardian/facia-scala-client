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
  val paid = "paid"

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

    val hashedTagIds: Seq[String] = content.tags.flatMap { tag =>
      md5(salt + tag.id)
    }

    if (trailMetaData.snapType.contains("link") && href.exists(ExternalLinks.external)) {
      ExternalLink
    } else if (hashedTagIds.contains("344ce3383665e23496bedd160675780c") // news/series/hsbc-files
      || hashedTagIds.contains("d36fa10d66bf5ff85894829d3829d9e1")      // news/series/panama-papers
      || hashedTagIds.contains("2920a7e21dc9f6fd0c008b50709c042f")      // us-news/homan-square
      || hashedTagIds.contains("ae4bd9f302c420d242a8da91a47a9ddd")      // ...
      || hashedTagIds.contains("f336cb0e0941d3d3d275e6451babca89")      // uk-news/series/the-new-world-of-work
      || hashedTagIds.contains("6f92a967c35b72ef4867f23ba88c95a1")      // world/series/the-new-arrivals
      || hashedTagIds.contains("01c74eb3a6d3c978e26d2c8f19f9b6a3")      // ...
      || hashedTagIds.contains("4dae5700e6b6fdf66d1567769b41c1c2")      // news/series/nauru-files
      || hashedTagIds.contains("9d89e70b7d99e776ffb741c0b9ab8854")      // us-news/series/counted-us-police-killings
      || hashedTagIds.contains("7037b49de72275eb72b73a111da31849")      // australia-news/series/healthcare-in-detention
      || hashedTagIds.contains("efb4e63b9a3a926314724b45764a5a5a") ) {  // society/series/this-is-the-nhs
      SpecialReport
    } else if (content.isPaid) {
      Paid
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

case object Paid extends CardStyle {
  val toneString = CardStyle.paid
}

case object DefaultCardstyle extends CardStyle {
  val toneString = CardStyle.news
}
