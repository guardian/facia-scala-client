package com.gu.facia.api.integration

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.facia.api.FAPI
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}
import lib.IntegrationTestConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{Ignore, FreeSpec, ShouldMatchers}

@Ignore
class IntegrationTest extends FreeSpec with ShouldMatchers with ScalaFutures with IntegrationTestConfig {
  implicit val patience = PatienceConfig(Span(500, Millis))

  val amazonS3Client = new AmazonS3Client(new BasicAWSCredentials("key", "secret-key"))
  implicit val apiClient: ApiClient = ApiClient("aws-frontend-store", "DEV", AmazonSdkS3Client(amazonS3Client))

  "getFronts" - {
    "should return a set of Front instances from the fronts JSON" in {
      FAPI.getFronts().asFuture.futureValue.fold(
        err => fail(s"expected fronts, got error $err"),
        fronts => fronts.size should be > 0
      )
    }
  }

  "frontsForPath" - {
    "should return the front for the given path" in {
      FAPI.frontForPath("uk").asFuture.futureValue.fold(
        err => fail(s"expected front, got error $err"),
        front => front.id should equal("uk")
      )
    }
  }

  "getCollection" - {
    "should return the collection at a given path" ignore {}
  }
}
