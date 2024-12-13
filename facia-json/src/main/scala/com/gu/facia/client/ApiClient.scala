package com.gu.facia.client

import com.gu.etagcaching.FreshnessPolicy.AlwaysWaitForRefreshedValue
import com.gu.etagcaching.aws.s3.{ObjectId, S3ByteArrayFetching}
import com.gu.etagcaching.fetching.Fetching
import com.gu.etagcaching.{ConfigCache, ETagCache}
import com.gu.facia.client.models.{CollectionJson, ConfigJson}
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

trait ApiClient {
  def config: Future[ConfigJson]

  def collection(id: String): Future[Option[CollectionJson]]
}

object ApiClient {
  private val Encoding = "utf-8"

  /**
   * Legacy constructor for creating a client that does not support caching. Use `ApiClient.withCaching()` instead.
   */
  def apply(
    bucket: String,
    environment: String, // e.g., CODE, PROD, DEV ...
    s3Client: S3Client
  )(implicit executionContext: ExecutionContext): ApiClient = new ApiClient {
    val env: Environment = Environment(environment)

    private def retrieve[A: Format](key: String): Future[Option[A]] = s3Client.get(bucket, key).map(translateFaciaResult[A](_))

    def config: Future[ConfigJson] =
      retrieve[ConfigJson](env.configS3Path).map(getOrWarnAboutMissingConfig)

    def collection(id: String): Future[Option[CollectionJson]] =
      retrieve[CollectionJson](env.collectionS3Path(id))
  }

  private def getOrWarnAboutMissingConfig(configOpt: Option[ConfigJson]): ConfigJson =
    configOpt.getOrElse(throw BackendError("Config was missing!! NOT GOOD!"))

  private def translateFaciaResult[B: Format](faciaResult: FaciaResult): Option[B] = faciaResult match {
    case FaciaSuccess(bytes) => Some(parseBytes(bytes))
    case FaciaNotAuthorized(message) => throw BackendError(message)
    case FaciaNotFound(_) => None
    case FaciaUnknownError(message) => throw BackendError(message)
  }

  private def parseBytes[B: Format](bytes: Array[Byte]): B =
    Json.fromJson[B](Json.parse(new String(bytes, Encoding))) getOrElse {
      throw JsonDeserialisationError(s"Could not deserialize JSON")
    }

  /**
   * @param s3Fetching see scaladoc on `S3ByteArrayFetching` i.e. use `S3ObjectFetching.byteArraysWith(s3AsyncClient)`
   */
  def withCaching(
    bucket: String,
    environment: Environment,
    s3Fetching: S3ByteArrayFetching,
    configureCollectionCache: ConfigCache = _.maximumSize(10000) // at most 1GB RAM worst case
  )(implicit ec: ExecutionContext): ApiClient = new ApiClient {
    private val fetching =
      s3Fetching.keyOn[String](path => ObjectId(bucket, path))

    def eTagCache[B: Format](configureCache: ConfigCache) = new ETagCache(
      fetching.thenParsing(parseBytes[B]),
      AlwaysWaitForRefreshedValue,
      configureCache
    )

    private val configCache = eTagCache[ConfigJson](_.maximumSize(1))
    private val collectionCache = eTagCache[CollectionJson](configureCollectionCache)

    override def config: Future[ConfigJson] =
      configCache.get(environment.configS3Path).map(getOrWarnAboutMissingConfig)

    override def collection(id: String): Future[Option[CollectionJson]] =
      collectionCache.get(environment.collectionS3Path(id))
  }
}
