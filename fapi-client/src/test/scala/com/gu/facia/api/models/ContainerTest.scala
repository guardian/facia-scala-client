package com.gu.facia.api.models

import org.scalatest.{FlatSpec, Matchers}

class ContainerTest extends FlatSpec with Matchers {

  "Container" should "extract the correct type" in {
    val collectionTypeOne = "dynamic/fast"
    CollectionType.fromCollectionType(collectionTypeOne) should be(Dynamic(DynamicFast))

    val collectionTypeTwo = "dynamic/slow"
    CollectionType.fromCollectionType(collectionTypeTwo) should be(Dynamic(DynamicSlow))

    val collectionTypeThree = "nav/media-list"
    CollectionType.fromCollectionType(collectionTypeThree) should be(NavMediaList)
  }

  it should "fall back to a default for something unrecognisable" in {
    val badCollectionType = "Ashausas"

    val container = CollectionType.fromCollectionType(badCollectionType)

    container should be (Fixed(FixedSmallSlowVI))
    container should be (CollectionType.default)
  }

}
