package com.gu.facia.client

import scala.concurrent.Future

/**
 * Legacy class for mocking in tests, but also previously used to allow library users to define a properly
 * asynchronous S3 client (back when the one in the AWS SDK was synchronous only).
 *
 * Note that use of `S3Client` is now discouraged, as `facia-scala-client` now supports caching using the
 * `etag-caching` library, which provides its own more powerful abstraction for fetching & parsing data, and
 * `com.gu.etagcaching.aws.sdkv2.s3.S3ObjectFetching`, a ready-made implementation of fetching that includes
 * support for ETags & Conditional Requests, decoding S3 exceptions, etc.
 */
trait S3Client {
  def get(bucket: String, path: String): Future[FaciaResult]
}
