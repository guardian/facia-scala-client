package com.gu.facia.client

import scala.concurrent.{ExecutionContext, Future}
import com.gu.facia.client.models.{Collection, Config}
import play.api.libs.json.{Format, Json}

object ApiClient {
  val Encoding = "utf-8"
}

case class ApiClient(
    bucket: String,
    /** e.g., CODE, PROD, DEV ... */
    environment: String,
    s3Client: S3Client
)(implicit executionContext: ExecutionContext) {
  import ApiClient._

  private def retrieve[A: Format](key: String) = s3Client.get(bucket, key) map {
    case FaciaSuccess(bytes) =>
        Json.fromJson[A](Json.parse(new String(bytes, Encoding))) getOrElse {
          throw new JsonDeserialisationError(s"Could not deserialize JSON in $bucket, $key")
        }
    case FaciaNotAuthorized(message) => throw new BackendError(message)
    case FaciaNotFound(message)  => throw new BackendError(message)
    case FaciaUnknownError(message)  => throw new BackendError(message)
  }

  def config: Future[Config] =
    retrieve[Config](s"$environment/frontsapi/config/config.json")

  def collection(id: String): Future[Collection] =
    retrieve[Collection](s"$environment/frontsapi/collection/$id/collection.json")
}
