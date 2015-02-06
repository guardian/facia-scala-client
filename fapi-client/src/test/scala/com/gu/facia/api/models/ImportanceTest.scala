package com.gu.facia.api.models

import com.gu.facia.client.models.CollectionConfigJson
import org.scalatest.{Matchers, FlatSpec}

class ImportanceTest extends FlatSpec with Matchers {

  def collectionConfigJsonWithImportance(importance: Option[String]) =
    CollectionConfigJson.withDefaults(importance = importance)

  val criticalImportance = collectionConfigJsonWithImportance(Option("critical"))
  val importanceImportance = collectionConfigJsonWithImportance(Option("important"))
  val nonsenseImportance = collectionConfigJsonWithImportance(Option("nonsense"))
  val nonexistantImportance = collectionConfigJsonWithImportance(None)

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
