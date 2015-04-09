package com.gu.facia.api.models

import com.gu.facia.client.models.CollectionConfigJson

case class Groups(groups: List[String])

case class CollectionConfig(
    displayName: Option[String],
    apiQuery: Option[String],
    collectionType: String,
    href: Option[String],
    groups: Option[Groups],
    uneditable: Boolean,
    showTags: Boolean,
    showSections: Boolean,
    hideKickers: Boolean,
    showDateHeader: Boolean,
    showLatestUpdate: Boolean,
    excludeFromRss: Boolean,
    showTimestamps: Boolean)

object CollectionConfig {
  val DefaultCollectionType = "fixed/small/slow-VI"

  val empty = CollectionConfig(
    displayName = None,
    apiQuery = None,
    collectionType = DefaultCollectionType,
    href = None,
    groups = None,
    uneditable = false,
    showTags = false,
    showSections = false,
    hideKickers = false,
    showDateHeader = false,
    showLatestUpdate = false,
    excludeFromRss = false,
    showTimestamps = false)

  def fromCollectionJson(collectionJson: CollectionConfigJson): CollectionConfig =
    CollectionConfig(
      collectionJson.displayName,
      collectionJson.apiQuery,
      collectionJson.collectionType getOrElse DefaultCollectionType,
      collectionJson.href,
      collectionJson.groups.map(Groups),
      collectionJson.uneditable.getOrElse(false),
      collectionJson.showTags.getOrElse(false),
      collectionJson.showSections.getOrElse(false),
      collectionJson.hideKickers.getOrElse(false),
      collectionJson.showDateHeader.getOrElse(false),
      collectionJson.showLatestUpdate.getOrElse(false),
      collectionJson.excludeFromRss.exists(identity),
      collectionJson.showTimestamps.exists(identity))
}