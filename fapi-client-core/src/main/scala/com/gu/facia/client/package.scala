package com.gu.facia

import com.gu.etagcaching.aws.s3.ObjectId
import com.gu.etagcaching.fetching.Fetching

package object client {
  /**
   * AWS SDK v2
   *
   * Add "com.gu.etag-caching" %% "aws-s3-sdk-v2" as dependency,
   * import com.gu.etagcaching.aws.sdkv2.s3.S3ObjectFetching
   *
   * S3ObjectFetching.byteArrayWith(s3AsyncClient)
   */
  type S3FetchBehaviour = Fetching[ObjectId, Array[Byte]]
}
