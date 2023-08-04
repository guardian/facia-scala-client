package com.gu.facia.client

import scala.concurrent.Future

/** For mocking in tests, but also to allow someone to define a properly asynchronous S3 client. (The one in the AWS
 * SDK is unfortunately synchronous only.)
 */
trait S3Client {
  def get(bucket: String, path: String): Future[FaciaResult]
}
