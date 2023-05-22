package com.gu.facia.client.lib

import com.gu.facia.client.models.{CollectionJson, ConfigJson}
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}
import play.api.libs.json.{Format, Json}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Amazon {
  val amazonS3Client = S3AsyncClient.builder()
    .region(Region.EU_WEST_1)
    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("key","pass")))
    .build()
}

class ApiTestClient extends ApiClient("bucket", "DEV", AmazonSdkS3Client(Amazon.amazonS3Client)) with ResourcesHelper {

  private def retrieve[A: Format](key: String): Future[Option[A]] =
    Future.successful(slurp(key).map(Json.parse).flatMap(_.asOpt[A]))

  override def config: Future[ConfigJson] =
    retrieve[ConfigJson](s"$environment/frontsapi/config/config.json").map(_.get)

  override def collection(id: String): Future[Option[CollectionJson]] =
    retrieve[CollectionJson](s"$environment/frontsapi/collection/$id/collection.json")
}
