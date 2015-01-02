package com.gu.facia.client

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.AmazonS3Exception
import org.apache.commons.io.IOUtils
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.Try

/** For mocking in tests, but also to allow someone to define a properly asynchronous S3 client. (The one in the AWS
  * SDK is unfortunately synchronous only.)
  */
trait S3Client {
  def get(bucket: String, path: String): Future[FaciaResult]
}

case class AmazonSdkS3Client(client: AmazonS3Client)(implicit executionContext: ExecutionContext) extends S3Client {
  def get(bucket: String, path: String): Future[FaciaResult] = Future {
    blocking {
      Try(IOUtils.toByteArray(client.getObject(bucket, path).getObjectContent))
        .map(FaciaSuccess.apply)
        .recover(recoverFromAmazonSdkExceptions)
        .get
    }
  }

  private def recoverFromAmazonSdkExceptions: PartialFunction[Throwable, FaciaResult] = {
    case ex: AmazonS3Exception if ex.getErrorCode == "AccessDenied" => FaciaNotAuthorized(ex.getMessage)
    case ex: AmazonS3Exception if ex.getErrorCode == "NoSuchKey" => FaciaNotFound(ex.getMessage)
    case ex: AmazonS3Exception => FaciaUnknownError(ex.getMessage)
  }
}

object AmazonSdkS3Client {
  def default(implicit executionContext: ExecutionContext) = {
    val client = new AmazonS3Client()
    client.setRegion(Region.getRegion(Regions.EU_WEST_1))
    AmazonSdkS3Client(client)
  }
}