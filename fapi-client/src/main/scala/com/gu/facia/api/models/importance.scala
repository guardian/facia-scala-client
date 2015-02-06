package com.gu.facia.api.models

import com.gu.facia.client.models.CollectionConfigJson

trait Importance

object Importance {
  def fromCollectionConfigJson(collectionConfig: CollectionConfigJson): Importance =
    collectionConfig.importance match {
      case Some("critical") => Critical
      case Some("important") => Important
      case _ => DefaultImportance
    }
}

case object Critical extends Importance
case object Important extends Importance
case object DefaultImportance extends Importance

