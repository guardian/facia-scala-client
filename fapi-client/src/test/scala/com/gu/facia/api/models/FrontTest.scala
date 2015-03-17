package com.gu.facia.api.models

import com.gu.facia.client.models.FrontJson
import org.scalatest._
import org.scalatest.mock.MockitoSugar

class FrontTest extends FreeSpec with ShouldMatchers with MockitoSugar with OneInstancePerTest {
  "fromFrontJson" - {
    "when generating the canonical field" - {
      val frontJson = FrontJson(
        collections = List("collection1", "collection2", "collection3", "collection4"),
        None, None, None, None, None, None, None, None, None, None, None, None
      )

      "uses the fronts config field if present" in {
        val frontJsonWithCanonical = frontJson.copy(canonical = Some("collection2"))
        Front.fromFrontJson("frontId", frontJsonWithCanonical).canonicalCollection should equal("collection2")
      }

      "in the absence of a fronts config field, takes the first present collection" in {
        Front.fromFrontJson("frontId", frontJson).canonicalCollection should equal("collection1")
      }
    }
  }
}
