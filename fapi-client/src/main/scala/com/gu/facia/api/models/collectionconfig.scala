package com.gu.facia.api.models

import com.gu.facia.api.models.CollectionConfig.AspectRatio.{Landscape53, Landscape54, Landscape54Collections, Portrait45, PortraitCollections, Square}
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
    frontsToolSettings: Option[FrontsToolSettings]
   )

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
    frontsToolSettings = None
  )

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
      collectionJson.frontsToolSettings
    )

  sealed trait AspectRatio {
    def label: String
  }

  object AspectRatio {
    case object Portrait45 extends AspectRatio {
      val label = "4:5"
    }

    case object Landscape53 extends AspectRatio {
      val label = "5:3"
    }

    case object Landscape54 extends AspectRatio {
      val label = "5:4"
    }

    case object Square extends AspectRatio {
      val label = "1:1"
    }

    val Landscape54Collections = List(
      "flexible/special",
      "flexible/general",
      "scrollable/small",
      "scrollable/medium",
      "static/medium/4",
    )

    val PortraitCollections = List(
      "scrollable/feature",
      "static/feature/2",
    )
  }

  def getAspectRatio(collectionConfig: CollectionConfig): AspectRatio = {
    collectionConfig.collectionType match {
      case _ if PortraitCollections.contains(collectionConfig.collectionType) => Portrait45
      case _ if Landscape54Collections.contains(collectionConfig.collectionType) => Landscape54
      case "scrollable/highlights" => Square
      case _ => Landscape53
    }
  }
}
