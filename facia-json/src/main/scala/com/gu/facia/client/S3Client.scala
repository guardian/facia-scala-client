package com.gu.facia.client

import org.apache.commons.io.IOUtils
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.core.internal.async.ByteArrayAsyncResponseTransformer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse, NoSuchKeyException, S3Exception}
import software.amazon.awssdk.services.s3.{S3AsyncClient => AWSS3Client}

import scala.concurrent.{ExecutionContext, Future, Promise, blocking}
import scala.util.Try

/** For mocking in tests, but also to allow someone to define a properly asynchronous S3 client. (The one in the AWS
  * SDK is unfortunately synchronous only.)
  */
trait S3Client {
  def get(bucket: String, path: String): Future[FaciaResult]
}

class AmazonSdkS3Client(client:AWSS3Client)(implicit executionContext: ExecutionContext) extends S3Client {
  def get(bucket: String, path: String): Future[FaciaResult] = {
    val req = GetObjectRequest.builder()
      .bucket(bucket)
      .key(path)
      .build()

    val transformer:AsyncResponseTransformer[GetObjectResponse, ResponseBytes[GetObjectResponse]] = AsyncResponseTransformer.toBytes()

    //On scala 2.13 I can just do this with scala.jdk.FutureConverters, but we must cross-compile for 2.12 as well.
    val completionPromise = Promise[Array[Byte]]()
    client
      .getObject(req, transformer)
      .whenComplete((t: ResponseBytes[GetObjectResponse], u: Throwable) => {
        (Option(t), Option(u)) match {
          case (_, Some(error))=>
            completionPromise.failure(error)
          case (Some(response), _)=>
            completionPromise.success(response.asByteArray())
          case (None, None) =>
            //This should never happen
            completionPromise.failure(new RuntimeException("S3 client should have given us either a response or an error but we got neither!"))
        }
      })

    completionPromise.future.map(FaciaSuccess.apply).recover(recoverFromAmazonSdkExceptions)
  }

  private def recoverFromAmazonSdkExceptions: PartialFunction[Throwable, FaciaResult] = {
    case ex: S3Exception if ex.awsErrorDetails().errorCode() == "AccessDenied" => FaciaNotAuthorized(ex.getMessage)
    case ex: NoSuchKeyException => FaciaNotFound(ex.getMessage)
    case ex: AwsServiceException => FaciaUnknownError(ex.getMessage)
  }
}
object AmazonSdkS3Client {
  def default(implicit executionContext: ExecutionContext): AmazonSdkS3Client = apply

  def apply(implicit executionContext: ExecutionContext): AmazonSdkS3Client = {
    val client = AWSS3Client.builder().region(Region.EU_WEST_1).build()
    new AmazonSdkS3Client(client)
  }

  def apply(client: AWSS3Client)(implicit ec:ExecutionContext) = new AmazonSdkS3Client(client)

}