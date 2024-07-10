package com.gu.facia.api.models

import com.gu.facia.client.models.{AnyPlatform, Backfill, CollectionConfigJson, CollectionPlatform, DisplayHintsJson, FrontsToolSettings, Metadata, TargetedTerritory}

case class Groups(groups: List[String])

case class DisplayHints(maxItemsToDisplay: Option[Int])

object DisplayHints {
  def fromDisplayHintsJson(displayHintsJson: DisplayHintsJson): DisplayHints = DisplayHints(
    maxItemsToDisplay = displayHintsJson.maxItemsToDisplay
  )
}


case class CollectionConfig(
    displayName: Option[String],
    backfill: Option[Backfill],
    metadata: Option[List[Metadata]],
    collectionType: String,
    href: Option[String],
    description: Option[String],
    groups: Option[Groups],
    uneditable: Boolean,
    showTags: Boolean,
    showSections: Boolean,
    hideKickers: Boolean,
    showDateHeader: Boolean,
    showLatestUpdate: Boolean,
    excludeFromRss: Boolean,
    showTimestamps: Boolean,
    hideShowMore: Boolean,
    displayHints: Option[DisplayHints],
    userVisibility: Option[String],
    targetedTerritory: Option[TargetedTerritory],
    platform: CollectionPlatform = AnyPlatform,
    frontsToolSettings: Option[FrontsToolSettings])

object CollectionConfig {
  val DefaultCollectionType = "fixed/small/slow-IV"

  val empty = CollectionConfig(
    displayName = None,
    backfill = None,
    metadata = None,
    collectionType = DefaultCollectionType,
    href = None,
    description = None,
    groups = None,
    uneditable = false,
    showTags = false,
    showSections = false,
    hideKickers = false,
    showDateHeader = false,
    showLatestUpdate = false,
    excludeFromRss = false,
    showTimestamps = false,
    hideShowMore = false,
    displayHints = None,
    userVisibility = None,
    targetedTerritory = None,
    platform = AnyPlatform,
    frontsToolSettings = None)

  def fromCollectionJson(collectionJson: CollectionConfigJson): CollectionConfig =
    CollectionConfig(
      collectionJson.displayName,
      collectionJson.backfill,
      collectionJson.metadata,
      collectionJson.collectionType getOrElse DefaultCollectionType,
      collectionJson.href,
      collectionJson.description,
      collectionJson.groups.map(Groups),
      collectionJson.uneditable.exists(identity),
      collectionJson.showTags.exists(identity),
      collectionJson.showSections.exists(identity),
      collectionJson.hideKickers.exists(identity),
      collectionJson.showDateHeader.exists(identity),
      collectionJson.showLatestUpdate.exists(identity),
      collectionJson.excludeFromRss.exists(identity),
      collectionJson.showTimestamps.exists(identity),
      collectionJson.hideShowMore.exists(identity),
      collectionJson.displayHints.map(DisplayHints.fromDisplayHintsJson),
      collectionJson.userVisibility,
      collectionJson.targetedTerritory,
      collectionJson.platform.getOrElse(AnyPlatform),
      collectionJson.frontsToolSettings)
}
