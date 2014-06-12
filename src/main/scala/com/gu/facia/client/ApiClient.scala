package com.gu.facia.client

import scala.concurrent.{ExecutionContext, Future}
import com.gu.facia.client.models.{Collection, Config}
import play.api.libs.json.{Reads, Json}

object ApiClient {
  val Encoding = "utf-8"
}

case class ApiClient(bucket: String, s3Client: S3Client)(implicit executionContext: ExecutionContext) {
  import ApiClient._

  private def retrieve[A: Reads](key: String) = s3Client.get(bucket, key) map { bytes: Array[Byte] =>
    Json.fromJson[A](Json.parse(new String(bytes, Encoding))) getOrElse {
      throw new JsonDeserialisationError(s"Could not deserialize JSON in $bucket, $key")
    }
  }

  def config: Future[Config] =
    retrieve[Config]("frontsapi/config/config.json")

  def collection(id: String): Future[Collection] =
    retrieve[Collection](s"frontsapi/collection/$id/collection.json")
}
