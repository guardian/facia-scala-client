package com.gu.facia.client

import com.amazonaws.services.s3.AmazonS3Client
import org.apache.commons.io.IOUtils
import scala.concurrent.{ExecutionContext, Future}

/** For mocking in tests, but also to allow someone to define a properly asynchronous S3 client. (The one in the AWS
  * SDK is unfortunately synchronous only.)
  */
trait S3Client {
  def get(bucket: String, path: String): Future[Array[Byte]]
}

case class AmazonSdkS3Client(client: AmazonS3Client)(implicit executionContext: ExecutionContext) {
  def get(bucket: String, path: String) = Future {
    IOUtils.toByteArray(client.getObject(bucket, path).getObjectContent)
  }
}

object AmazonSdkS3Client {
  def default(implicit executionContext: ExecutionContext) = AmazonSdkS3Client(new AmazonS3Client())
}