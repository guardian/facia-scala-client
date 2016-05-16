package com.gu.facia.api.models

import com.gu.facia.client.models.{Metadata, Backfill, CollectionConfigJson}

case class Groups(groups: List[String])

case class CollectionConfig(
    displayName: Option[String],
    backfill: Option[Backfill],
    collectionType: String,
    href: Option[String],
    description: Option[String],
    metadata: Option[List[Metadata]],
    groups: Option[Groups],
    uneditable: Boolean,
    showTags: Boolean,
    showSections: Boolean,
    hideKickers: Boolean,
    showDateHeader: Boolean,
    showLatestUpdate: Boolean,
    excludeFromRss: Boolean,
    showTimestamps: Boolean,
    hideShowMore: Boolean)

object CollectionConfig {
  val DefaultCollectionType = "fixed/small/slow-VI"

  val empty = CollectionConfig(
    displayName = None,
    backfill = None,
    collectionType = DefaultCollectionType,
    href = None,
    description = None,
    metadata = None,
    groups = None,
    uneditable = false,
    showTags = false,
    showSections = false,
    hideKickers = false,
    showDateHeader = false,
    showLatestUpdate = false,
    excludeFromRss = false,
    showTimestamps = false,
    hideShowMore = false)

  def fromCollectionJson(collectionJson: CollectionConfigJson): CollectionConfig =
    CollectionConfig(
      collectionJson.displayName,
      collectionJson.backfill,
      collectionJson.collectionType getOrElse DefaultCollectionType,
      collectionJson.href,
      collectionJson.description,
      collectionJson.metadata,
      collectionJson.groups.map(Groups),
      collectionJson.uneditable.exists(identity),
      collectionJson.showTags.exists(identity),
      collectionJson.showSections.exists(identity),
      collectionJson.hideKickers.exists(identity),
      collectionJson.showDateHeader.exists(identity),
      collectionJson.showLatestUpdate.exists(identity),
      collectionJson.excludeFromRss.exists(identity),
      collectionJson.showTimestamps.exists(identity),
      collectionJson.hideShowMore.exists(identity))
}
