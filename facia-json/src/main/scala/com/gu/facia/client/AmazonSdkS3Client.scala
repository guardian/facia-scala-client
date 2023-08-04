package com.gu.facia.client

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import org.apache.commons.io.IOUtils

import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.Try



case class AmazonSdkS3Client(client: AmazonS3)(implicit executionContext: ExecutionContext) extends S3Client {
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
    val client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1).build()
    AmazonSdkS3Client(client)
  }
}