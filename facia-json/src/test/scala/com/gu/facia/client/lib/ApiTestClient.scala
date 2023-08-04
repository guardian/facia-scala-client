package com.gu.facia.client.lib

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.gu.facia.client.models.{CollectionJson, ConfigJson}
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient, Environment}
import play.api.libs.json.{Format, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Amazon {
  val amazonS3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("key", "pass"))).build()
}

class ApiTestClient extends ApiClient with ResourcesHelper {
  private val environment = Environment.Dev

  private def retrieve[A: Format](key: String): Future[Option[A]] =
    Future.successful(slurp(key).map(Json.parse).flatMap(_.asOpt[A]))

  override def config: Future[ConfigJson] =
    retrieve[ConfigJson](environment.configS3Path).map(_.get)

  override def collection(id: String): Future[Option[CollectionJson]] =
    retrieve[CollectionJson](environment.collectionS3Path(id))
}
