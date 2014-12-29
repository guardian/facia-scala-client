package com.gu.facia.api.models

import com.gu.contentapi.client.model.Content
import com.gu.facia.client.models.CollectionJson

case class Collection(
  id: CollectionId,
  displayName: String,
  live: List[CuratedContent],
  draft: Option[List[CuratedContent]],
  updatedBy: String,
  updatedEmail: String,
  href: Option[String],
  apiQuery: Option[String],
  collectionType: CollectionType,
  groups: Option[List[Group]],
  uneditable: Boolean,
  showTags: Boolean,
  showSections: Boolean,
  hideKickers: Boolean,
  showDateHeader: Boolean,
  showLatestUpdate: Boolean
)

object Collection {

  def fromCollectionJsonConfigAndContent(id: CollectionId, collectionJson: CollectionJson, collectionConfig: CollectionConfig, content: Set[Content]): Collection = {
    // if content is not in the set it was most likely filtered out by the CAPI query, so exclude it
    // note that this does not currently deal with e.g. snaps
    val live = collectionJson.live.flatMap { trail =>
      content.find(_.id == trail.id).map { content =>
        FaciaContent.fromTrailAndContent(content, trail.safeMeta, collectionConfig)
      }
    }
    val draft = collectionJson.draft.map {
      _.flatMap { trail =>
        content.find(_.id == trail.id).map { content =>
          FaciaContent.fromTrailAndContent(content, trail.safeMeta, collectionConfig)
        }
      }
    }
    Collection(
      id,
      collectionJson.displayName.orElse(collectionConfig.displayName).getOrElse("untitled"),
      live,
      draft,
      collectionJson.updatedBy,
      collectionJson.updatedEmail,
      collectionJson.href.orElse(collectionConfig.href),
      collectionConfig.apiQuery,
      collectionConfig.collectionType,
      collectionConfig.groups.map(Group.fromGroups),
      collectionConfig.uneditable,
      collectionConfig.showTags,
      collectionConfig.showSections,
      collectionConfig.hideKickers,
      collectionConfig.showDateHeader,
      collectionConfig.showLatestUpdate
    )
  }
}

sealed trait Group
object Standard extends Group
object Big extends Group
object VeryBig extends Group
object Huge extends Group
object Group {
  def fromGroups(groups: Groups): List[Group] = {
    groups.groups.map {
      case "3" => Huge
      case "2" => VeryBig
      case "1" => Big
      case _ => Standard
    }
  }

  def toGroups(groups: List[Group]): Groups = {
    Groups(groups.map {
      case Huge => "3"
      case VeryBig => "2"
      case Big => "1"
      case Standard => "0"
    })
  }
}