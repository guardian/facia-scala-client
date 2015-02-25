package com.gu.facia.api.utils

import com.gu.contentapi.client.model.Content
import com.gu.facia.api.models.CollectionConfig
import com.gu.facia.client.models.{CollectionConfigJson, TrailMetaData}
import org.mockito.Mockito
import org.scalatest.mock.MockitoSugar
import org.scalatest.{OneInstancePerTest, OptionValues, ShouldMatchers, FreeSpec}
import org.mockito.Mockito._


class ItemKickerTest extends FreeSpec with ShouldMatchers with MockitoSugar with OptionValues with OneInstancePerTest {
  "fromContentAndTrail" - {
    val trailMetadata = Mockito.spy(TrailMetaData.empty)
    val content = mock[Content]
    val collectionConfig = Mockito.spy(CollectionConfig.fromCollectionJson(CollectionConfigJson.withDefaults()))
    val metaDataDefaults = MetadataDefaults.Default

    "should prefer item level custom kicker to collection level section kicker" in {
      when(trailMetadata.customKicker).thenReturn(Some("custom kicker"))
      when(trailMetadata.showKickerCustom).thenReturn(Some(true))
      val metaDataDefaultsWithShowKickerCustom = metaDataDefaults.copy(showKickerCustom = true)
      when(collectionConfig.showSections).thenReturn(true)

      ItemKicker.fromContentAndTrail(content, trailMetadata, metaDataDefaultsWithShowKickerCustom, Some(collectionConfig)).value shouldBe a [FreeHtmlKicker]
    }

    "should prefer item level section kicker to collection level tag kicker" in {
      when(collectionConfig.showTags).thenReturn(true)
      when(trailMetadata.showKickerSection).thenReturn(Some(true))
      val metaDataDefaultsWithShowKickerSection = metaDataDefaults.copy(showKickerSection = true)
      when(content.sectionId).thenReturn(Some("section"))
      when(content.sectionName).thenReturn(Some("Section"))

      ItemKicker.fromContentAndTrail(content, trailMetadata, metaDataDefaultsWithShowKickerSection, Some(collectionConfig)).value shouldBe a [SectionKicker]
    }
  }
}
