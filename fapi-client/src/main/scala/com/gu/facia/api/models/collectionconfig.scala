package com.gu.facia.api.models

import com.gu.facia.client.models.CollectionConfigJson

case class Groups(groups: List[String])

sealed trait DynamicContainerType
object DynamicFast extends DynamicContainerType
object DynamicSlow extends DynamicContainerType

sealed trait FixedContainerType
object FixedSmallSlowI extends FixedContainerType
object FixedSmallSlowII extends FixedContainerType
object FixedSmallSlowIII extends FixedContainerType
object FixedSmallSlowIV extends FixedContainerType
object FixedSmallSlowVhalf extends FixedContainerType
object FixedSmallSlowVthird extends FixedContainerType
object FixedSmallSlowVmpu extends FixedContainerType
object FixedSmallSlowVI extends FixedContainerType
object FixedSmallFastVIII extends FixedContainerType
object FixedSmallFastX extends FixedContainerType
object FixedMediumSlowVI extends FixedContainerType
object FixedMediumSlowVII extends FixedContainerType
object FixedMediumSlowVIII extends FixedContainerType
object FixedMediumSlowXIImpu extends FixedContainerType
object FixedMediumFastXI extends FixedContainerType
object FixedMediumFastXII extends FixedContainerType
object FixedLargeSlowXIV extends FixedContainerType
object FixedLargeFastXV extends FixedContainerType
object FixedThrasher extends FixedContainerType

object FixedContainerType {
  val all: Map[String, CollectionType] = Map(
    ("fixed/small/slow-I", Fixed(FixedSmallSlowI)),
    ("fixed/small/slow-II", Fixed(FixedSmallSlowII)),
    ("fixed/small/slow-III", Fixed(FixedSmallSlowIII)),
    ("fixed/small/slow-IV", Fixed(FixedSmallSlowIV)),
    ("fixed/small/slow-V-half", Fixed(FixedSmallSlowVhalf)),
    ("fixed/small/slow-V-third", Fixed(FixedSmallSlowVthird)),
    ("fixed/small/slow-V-mpu", Fixed(FixedSmallSlowVmpu)),
    ("fixed/small/slow-VI", Fixed(FixedSmallSlowVI)),
    ("fixed/small/fast-VIII", Fixed(FixedSmallFastVIII)),
    ("fixed/small/fast-X", Fixed(FixedSmallFastX)),
    ("fixed/medium/slow-VI", Fixed(FixedMediumSlowVI)),
    ("fixed/medium/slow-VII", Fixed(FixedMediumSlowVII)),
    ("fixed/medium/slow-VIII", Fixed(FixedMediumSlowVIII)),
    ("fixed/medium/slow-XII-mpu", Fixed(FixedMediumSlowXIImpu)),
    ("fixed/medium/fast-XI", Fixed(FixedMediumFastXI)),
    ("fixed/medium/fast-XII", Fixed(FixedMediumFastXII)),
    ("fixed/large/slow-XIV", Fixed(FixedLargeSlowXIV)),
    ("fixed/large/fast-XV", Fixed(FixedLargeFastXV)),
    ("fixed/thrasher", Fixed(FixedThrasher)))
}

sealed trait CollectionType
case class Dynamic(get: DynamicContainerType) extends CollectionType
case class Fixed(get: FixedContainerType) extends CollectionType
case object NavList extends CollectionType
case object NavMediaList extends CollectionType
case object MostPopular extends CollectionType

object CollectionType {
  val all = Map(
    ("dynamic/fast", Dynamic(DynamicFast)),
    ("dynamic/slow", Dynamic(DynamicSlow)),
    ("nav/list", NavList),
    ("nav/media-list", NavMediaList),
    ("news/most-popular", MostPopular)) ++ FixedContainerType.all

  val default = Fixed(FixedSmallSlowVI)

  def fromCollectionType(collectionType: String): CollectionType =
    all.getOrElse(collectionType, default)
}

case class CollectionConfig(
    displayName: Option[String],
    apiQuery: Option[String],
    collectionType: CollectionType,
    href: Option[String],
    groups: Option[Groups],
    uneditable: Boolean,
    showTags: Boolean,
    showSections: Boolean,
    hideKickers: Boolean,
    showDateHeader: Boolean,
    showLatestUpdate: Boolean)

object CollectionConfig {
  def fromCollectionJson(collectionJson: CollectionConfigJson): CollectionConfig =
    CollectionConfig(
      collectionJson.displayName,
      collectionJson.apiQuery,
      collectionJson.collectionType.map(CollectionType.fromCollectionType).getOrElse(CollectionType.default),
      collectionJson.href,
      collectionJson.groups.map(Groups),
      collectionJson.uneditable.getOrElse(false),
      collectionJson.showTags.getOrElse(false),
      collectionJson.showSections.getOrElse(false),
      collectionJson.hideKickers.getOrElse(false),
      collectionJson.showDateHeader.getOrElse(false),
      collectionJson.showLatestUpdate.getOrElse(false)
    )

}