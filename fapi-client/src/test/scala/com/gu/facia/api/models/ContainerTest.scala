package com.gu.facia.api.models

import org.scalatest.{FlatSpec, Matchers}

class ContainerTest extends FlatSpec with Matchers {

  "Container" should "extract the correct type" in {
    val collectionTypeOne = "dynamic/fast"
    Container.fromCollectionType(collectionTypeOne) should be(Dynamic(DynamicFast))

    val collectionTypeTwo = "dynamic/slow"
    Container.fromCollectionType(collectionTypeTwo) should be(Dynamic(DynamicSlow))

    val collectionTypeThree = "nav/media-list"
    Container.fromCollectionType(collectionTypeThree) should be(NavMediaList)
  }

  it should "fall back to a default for something unrecognisable" in {
    val badCollectionType = "Ashausas"

    val container = Container.fromCollectionType(badCollectionType)

    container should be (Fixed(FixedSmallSlowVI))
    container should be (Container.default)
  }

}
