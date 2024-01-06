package com.gu.facia.client.etagcaching.fetching

import com.gu.etagcaching.aws.s3.ObjectId
import com.gu.etagcaching.fetching.Fetching

trait S3FetchBehaviour {
  val fetching: Fetching[ObjectId, Array[Byte]]

  def fetchExceptionIndicatesContentMissing(t: Throwable): Boolean
}
