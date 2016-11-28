package com.gu.facia.client

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.facia.client.models.{CollectionJson, Trail}
import org.joda.time.{DateTime, Period}
import play.api.libs.json.{JsString}


case class S3ClientAndBucket(s3Client: AmazonS3Client, bucketName: String)


object MediaServiceClient {

  def generateThumbnailSecureUrl(imageSrc: String)(s3: S3ClientAndBucket) = {
    val idRegex = "[0-9a-z]{40}".r
    val mediaId = idRegex findFirstIn imageSrc

    val key = mediaId.map(id => id.take(6).split("").map(c => s"$c/").mkString+id)

    val expiryDate = DateTime.now.withPeriodAdded(Period.minutes(1), 5).toDate

    val signedUrl = key.map(s3.s3Client.generatePresignedUrl(s3.bucketName, _, expiryDate, HttpMethod.GET))

    signedUrl.map(_.toString).getOrElse("")
  }

  def addThumbnailUrlsToTrailList(list: List[Trail])(s3: S3ClientAndBucket) = {

    val updatedImageSrcs = list.map(trail => trail.copy(meta = trail.meta.map{ meta =>
      meta.copy(json =
        if (meta.imageSrc.isDefined) meta.json + ("imageSrcThumb" -> JsString(generateThumbnailSecureUrl(meta.imageSrc.get)(s3)))
        else meta.json )
    }))
    updatedImageSrcs
  }

  def addThumbnailsToCollection(collection: CollectionJson)(s3: S3ClientAndBucket) = {
    collection.copy(
      live = addThumbnailUrlsToTrailList(collection.live)(s3),
      draft = collection.draft.map(addThumbnailUrlsToTrailList(_)(s3))
    )
  }
}