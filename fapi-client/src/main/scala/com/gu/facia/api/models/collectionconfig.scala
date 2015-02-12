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
    showTimestamps: Boolean,
    importance: Importance)

object CollectionConfig {
  val DefaultCollectionType = "fixed/small/slow-VI"

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
      collectionJson.showTimestamps.exists(identity),
      Importance.fromCollectionConfigJson(collectionJson)
    )

  def fromCollection(collection: Collection): CollectionConfig =
    CollectionConfig(
      Some(collection.displayName),
      collection.apiQuery,
      collection.collectionType,
      collection.href,
      collection.groups.map(Group.toGroups),
      collection.uneditable,
      collection.showTags,
      collection.showSections,
      collection.hideKickers,
      collection.showDateHeader,
      collection.showLatestUpdate,
      collection.importance
    )
}