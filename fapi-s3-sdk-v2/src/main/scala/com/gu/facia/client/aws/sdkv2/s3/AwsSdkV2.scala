package com.gu.facia.client.aws.sdkv2.s3

import com.gu.etagcaching.aws.s3.ObjectId
import com.gu.etagcaching.aws.sdkv2.s3.S3ObjectFetching
import com.gu.etagcaching.aws.sdkv2.s3.response.Transformer.Bytes
import com.gu.etagcaching.fetching.Fetching
import com.gu.facia.client.etagcaching.fetching.S3FetchBehaviour
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.NoSuchKeyException

case class AwsSdkV2(s3AsyncClient: S3AsyncClient) extends S3FetchBehaviour {
  override val fetching: Fetching[ObjectId, Array[Byte]] =
    S3ObjectFetching(s3AsyncClient, Bytes).mapResponse(_.asByteArray())

  override def fetchExceptionIndicatesContentMissing(t: Throwable): Boolean = t match {
    case _: NoSuchKeyException => true
    case _ => false
  }
}
