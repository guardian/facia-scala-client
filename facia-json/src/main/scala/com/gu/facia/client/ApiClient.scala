package com.gu.facia.client

import com.gu.etagcaching.FreshnessPolicy.AlwaysWaitForRefreshedValue
import com.gu.etagcaching.aws.s3.ObjectId
import com.gu.etagcaching.{ConfigCache, ETagCache}
import com.gu.facia.client.etagcaching.fetching.S3FetchBehaviour
import com.gu.facia.client.models.{CollectionJson, ConfigJson}
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

trait ApiClient {
  def config: Future[ConfigJson]

  def collection(id: String): Future[Option[CollectionJson]]
}

object ApiClient {
  private val Encoding = "utf-8"

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
    configOpt getOrElse throwConfigMissingException()

  private def throwConfigMissingException() = throw BackendError("Config was missing!! NOT GOOD!")

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

  def withCaching(
    bucket: String,
    s3FetchBehaviour: S3FetchBehaviour,
    environment: Environment,
    configureCollectionCache: ConfigCache = _.maximumSize(10000) // at most 1GB RAM worst case
  )(implicit ec: ExecutionContext): ApiClient =
    new ApiClient {
      private val fetching =
        s3FetchBehaviour.fetching.keyOn[String](path => ObjectId(bucket, path))
      /* TODO - .suppressExceptionLoggingIf(s3FetchBehaviour.fetchExceptionIndicatesContentMissing)
       * Need to settle on an approach - see https://github.com/guardian/etag-caching/pull/32
       */

      def eTagCache[B: Format](configureCache: ConfigCache) = new ETagCache(
        fetching.thenParsing(parseBytes[B]),
        AlwaysWaitForRefreshedValue,
        configureCache
      )

      private val configCache = eTagCache[ConfigJson](_.maximumSize(1))
      private val collectionCache = eTagCache[CollectionJson](configureCollectionCache)

      override def config: Future[ConfigJson] = configCache.get(environment.configS3Path).recover {
        case t if s3FetchBehaviour.fetchExceptionIndicatesContentMissing(t) =>
          throwConfigMissingException()
      }

      override def collection(id: String): Future[Option[CollectionJson]] =
        collectionCache.get(environment.collectionS3Path(id)).map(Some(_)).recover {
          case t if {
            val boo = s3FetchBehaviour.fetchExceptionIndicatesContentMissing(t)
            println(s"Exception is content missing: $boo")
            boo
          } => None
          case t => throw BackendError(t.getMessage)
        }
    }
}
