package com.gu.facia.client

import com.gu.facia.client.lib.ResourcesHelper
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApiClientSpec extends AnyFlatSpec with Matchers with OptionValues with ScalaFutures with IntegrationPatience {
  object FakeS3Client extends S3Client with ResourcesHelper {
    override def get(bucket: String, path: String): Future[FaciaResult] = Future {
      slurpOrDie(path)
    }
  }

  val client: ApiClient = ApiClient("not used", "DEV", FakeS3Client)

  "ApiClient" should "fetch the config" in {
    val config = client.config.futureValue

    config.collections should have size 334
    config.fronts should have size 79
  }

  it should "fetch a collection" in {
    val collectionOpt = client.collection("2409-31b3-83df0-de5a").futureValue

    collectionOpt.value.live should have size 8
  }
}