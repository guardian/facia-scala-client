package com.gu.facia.client.lib

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.facia.client.models.{CollectionJson, ConfigJson}
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}
import play.api.libs.json.{Format, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Amazon {
  val amazonS3Client = new AmazonS3Client(new BasicAWSCredentials("key", "pass"))
}

class ApiTestClient extends ApiClient("bucket", "DEV", AmazonSdkS3Client(Amazon.amazonS3Client)) with ResourcesHelper {

  private def retrieve[A: Format](key: String): Future[A] =
    Future.successful(slurp(key).map(Json.parse).flatMap(_.asOpt[A]).get)

  override def config: Future[ConfigJson] =
    retrieve[ConfigJson](s"$environment/frontsapi/config/config.json")

  override def collection(id: String): Future[CollectionJson] =
    retrieve[CollectionJson](s"$environment/frontsapi/collection/$id/collection.json")
}
