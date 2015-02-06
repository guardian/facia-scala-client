package com.gu.facia.api.models

import com.gu.facia.client.models.CollectionConfigJson
import org.scalatest.{Matchers, FlatSpec}

class ImportanceTest extends FlatSpec with Matchers {

  val criticalImportance = CollectionConfigJson.withDefaults(importance = Option("critical"))
  val importanceImportance = CollectionConfigJson.withDefaults(importance = Option("important"))
  val nonsenseImportance = CollectionConfigJson.withDefaults(importance = Option("nonsense"))
  val nonexistantImportance = CollectionConfigJson.withDefaults(importance = None)

  "Importance object" should "resolve critically correctly" in {
    Importance.fromCollectionConfigJson(criticalImportance) should be (Critical)
  }

  it should "resolve important correctly" in {
    Importance.fromCollectionConfigJson(importanceImportance) should be (Important)
  }

  it should "resolve nonsense correctly" in {
    Importance.fromCollectionConfigJson(nonsenseImportance) should be (DefaultImportance)
  }

  it should "resolve nonexistant correctly" in {
    Importance.fromCollectionConfigJson(nonexistantImportance) should be (DefaultImportance)
  }

}
