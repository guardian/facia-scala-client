package com.gu.facia.client

import com.gu.facia.client.models.{ConfigJson, CollectionJson}
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

object ApiClient {
  val Encoding = "utf-8"
}

case class ApiClient(
    bucket: String,
    /** e.g., CODE, PROD, DEV ... */
    environment: String,
    s3Client: S3Client
)(implicit executionContext: ExecutionContext) {
  import com.gu.facia.client.ApiClient._

  private def retrieve[A: Format](key: String): Future[Option[A]] = s3Client.get(bucket, key) map {
    case FaciaSuccess(bytes) =>
        Some(Json.fromJson[A](Json.parse(new String(bytes, Encoding))) getOrElse {
          throw new JsonDeserialisationError(s"Could not deserialize JSON in $bucket, $key")
        })
    case FaciaNotAuthorized(message) => throw new BackendError(message)
    case FaciaNotFound(_)  => None
    case FaciaUnknownError(message)  => throw new BackendError(message)
  }

  def config: Future[ConfigJson] =
    retrieve[ConfigJson](s"$environment/frontsapi/config/config.json").map(_ getOrElse {
      throw new BackendError("Config was missing!! OH MY GOD")
    })

  def collection(id: String): Future[Option[CollectionJson]] =
    retrieve[CollectionJson](s"$environment/frontsapi/collection/$id/collection.json")
}
