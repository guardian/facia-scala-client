package com.gu.facia.client

import com.gu.etagcaching.aws.s3.{ObjectId, S3ByteArrayFetching}
import com.gu.etagcaching.fetching.{ETaggedData, Fetching, Missing, MissingOrETagged}
import com.gu.facia.client.lib.ResourcesHelper
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.hashing.MurmurHash3

/**
 * This is used only for testing, it's a dummy implementation of S3ByteArrayFetching that
 * just loads blobs from the `src/test/resources` folder, rather than hitting S3.
 */
object FakeS3Fetching extends S3ByteArrayFetching with ResourcesHelper {
  private def pretendETagFor(bytes: Array[Byte]): String = MurmurHash3.bytesHash(bytes).toHexString

  override def fetch(objectId: ObjectId): Future[MissingOrETagged[Array[Byte]]] = Future.successful {
    slurpBytes(objectId.key).fold(Missing: MissingOrETagged[Array[Byte]]) { bytes =>
      ETaggedData(pretendETagFor(bytes), bytes)
    }
  }

  override def fetchOnlyIfETagChanged(objectId: ObjectId, oldETag: String): Future[Option[MissingOrETagged[Array[Byte]]]] = {
    fetch(objectId).map {
      case taggedData: ETaggedData[Array[Byte]] =>
        Option.unless(oldETag == taggedData.eTag)(taggedData) // simulate a Not-Modified response, if there's no change in ETag
      case x => Some(x)
    }(ExecutionContext.parasitic)
  }
}

class ApiClientSpec extends AnyFlatSpec with Matchers with OptionValues with ScalaFutures with IntegrationPatience {
  import scala.concurrent.ExecutionContext.Implicits.global

  object FakeS3Client extends S3Client with ResourcesHelper {
    override def get(bucket: String, path: String): Future[FaciaResult] = Future {
      slurpOrDie(path)
    }
  }

  val bucketName = "not used"
  val legacyClient: ApiClient = ApiClient(bucketName, "DEV", FakeS3Client)
  val cachingClient: ApiClient = ApiClient.withCaching(bucketName, Environment.Dev, FakeS3Fetching)

  // Here we're testing that both the legacy client & the new caching client satisfy the same test criteria
  for ((name, client) <- Map("legacy" -> legacyClient, "caching" -> cachingClient)) {
    s"$name ApiClient" should "fetch the config" in {
      val config = client.config.futureValue

      config.collections should have size 334
      config.fronts should have size 79
    }

    it should "fetch a collection" in {
      val collectionOpt = cachingClient.collection("2409-31b3-83df0-de5a").futureValue

      collectionOpt.value.live should have size 8
    }
  }
}