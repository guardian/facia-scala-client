package com.gu.facia.client

import com.gu.etagcaching.aws.s3.ObjectId
import com.gu.etagcaching.fetching.{ETaggedData, Fetching, Missing, MissingOrETagged}
import com.gu.facia.client.lib.ResourcesHelper
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.hashing.MurmurHash3

object FakeS3Fetching extends Fetching[ObjectId, Array[Byte]] with ResourcesHelper {
  private def pretendETagFor(bytes: Array[Byte]): String = MurmurHash3.bytesHash(bytes).toHexString

  override def fetch(objectId: ObjectId)(implicit ec: ExecutionContext): Future[MissingOrETagged[Array[Byte]]] = Future {
    slurpBytes(objectId.key).fold(Missing: MissingOrETagged[Array[Byte]]) { bytes =>
      ETaggedData(pretendETagFor(bytes), bytes)
    }
  }

  override def fetchOnlyIfETagChanged(objectId: ObjectId, oldETag: String)(implicit ec: ExecutionContext): Future[Option[MissingOrETagged[Array[Byte]]]] = {
    fetch(objectId).map {
      case taggedData: ETaggedData[_] =>
        Option.unless(oldETag == taggedData.eTag)(taggedData) // simulate a Not-Modified response, if there's no change in ETag
      case x => Some(x)
    }
  }
}

class ApiClientSpec extends AnyFlatSpec with Matchers with OptionValues with ScalaFutures with IntegrationPatience {
  import scala.concurrent.ExecutionContext.Implicits.global

  object FakeS3Client extends S3Client with ResourcesHelper {
    override def get(bucket: String, path: String): Future[FaciaResult] = Future {
      slurpOrDie(path)
    }
  }

  val legacyClient: ApiClient = ApiClient("not used", "DEV", FakeS3Client)
  val cachingClient: ApiClient = ApiClient.withCaching("not used", Environment.Dev, FakeS3Fetching)

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