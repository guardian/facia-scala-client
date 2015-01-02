package com.gu.facia.client

import org.specs2.mutable.Specification
import scala.concurrent.Future
import scala.concurrent.Await
import com.gu.facia.client.lib.ResourcesHelper
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class ApiClientSpec extends Specification {
  object FakeS3Client extends S3Client with ResourcesHelper {
    override def get(bucket: String, path: String): Future[FaciaResult] = Future {
      slurpOrDie(path)
    }
  }

  val client = ApiClient("not used", "DEV", FakeS3Client)

  "ApiClient" should {
    "fetch the config" in {
      /** Nasty. PlaySpecification gives you a way of avoiding using Await ... TODO, maybe import that? */
      val config = Await.result(client.config, Duration.Inf)

      (config.collections.size mustEqual 334) and (config.fronts.size mustEqual 79)
    }

    "fetch a collection" in {
      val collection = Await.result(client.collection("2409-31b3-83df0-de5a"), Duration.Inf)

      collection must beSome.which(_.live must haveLength(8))
    }
  }
}