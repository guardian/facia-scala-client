package com.gu.facia.api.utils

import com.gu.contentapi.client.model.{Tag, Content}

object ContentApiUtils {

  implicit class RichContent(content: Content) {
    private def tagsOfType(tagType: String): Seq[Tag] = content.tags.filter(_.`type` == tagType)

    lazy val keywords: Seq[Tag] = tagsOfType("keyword")
    lazy val nonKeywordTags: Seq[Tag] = content.tags.filterNot(_.`type` == "keyword")
    lazy val contributors: Seq[Tag] = tagsOfType("contributor")
    lazy val isContributorPage: Boolean = contributors.nonEmpty
    lazy val series: Seq[Tag] = tagsOfType("series")
    lazy val blogs: Seq[Tag] = tagsOfType("blog")
    lazy val tones: Seq[Tag] = tagsOfType("tone")
    lazy val types: Seq[Tag] = tagsOfType("type")

    lazy val isLiveBlog: Boolean = tones.exists(t => Tags.liveMappings.contains(t.id))
    lazy val isComment = tones.exists(t => Tags.commentMappings.contains(t.id))
    lazy val isFeature = tones.exists(t => Tags.featureMappings.contains(t.id))
    lazy val isReview = tones.exists(t => Tags.reviewMappings.contains(t.id))
    lazy val isMedia = types.exists(t => Tags.mediaTypes.contains(t.id))
    lazy val isAnalysis = tones.exists(_.id == Tags.Analysis)
    lazy val isPodcast = isAudio && (types.exists(_.id == Tags.Podcast) || content.tags.exists(_.podcast.isDefined))
    lazy val isAudio = types.exists(_.id == Tags.Audio)
    lazy val isEditorial = tones.exists(_.id == Tags.Editorial)
    lazy val isCartoon = types.exists(_.id == Tags.Cartoon)
    lazy val isLetters = tones.exists(_.id == Tags.Letters)
    lazy val isCrossword = types.exists(_.id == Tags.Crossword)

    lazy val isArticle: Boolean = content.tags.exists { _.id == Tags.Article }
    lazy val isSudoku: Boolean = content.tags.exists { _.id == Tags.Sudoku } || content.tags.exists(t => t.id == "lifeandstyle/series/sudoku")
    lazy val isGallery: Boolean = content.tags.exists { _.id == Tags.Gallery }
    lazy val isVideo: Boolean = content.tags.exists { _.id == Tags.Video }
    lazy val isPoll: Boolean = content.tags.exists { _.id == Tags.Poll }
    lazy val isImageContent: Boolean = content.tags.exists { tag => List("type/cartoon", "type/picture", "type/graphic").contains(tag.id) }
    lazy val isInteractive: Boolean = content.tags.exists { _.id == Tags.Interactive }
  }
}
