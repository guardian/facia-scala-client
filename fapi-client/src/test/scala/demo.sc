import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.FAPI
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}
import concurrent.duration._

import scala.concurrent.Await

// demo config
implicit val capiClient = new GuardianContentClient(apiKey = "test")
private val amazonS3Client = new AmazonS3Client(new BasicAWSCredentials("key", "secret-key"))
implicit val apiClient: ApiClient = ApiClient("aws-frontend-store", "DEV", AmazonSdkS3Client(amazonS3Client))
implicit val executionContext = scala.concurrent.ExecutionContext.global

val frontResult = FAPI.frontForPath("uk/business").fold(
  { err => s"error: $err"},
  { front => s"found front: $front" }
)
Await.result(frontResult, 1.second)

val frontsResult = FAPI.getFronts().fold(
  { err => s"error fetching fronts: $err"},
  { fronts => s"found fronts: $fronts" }
)
Await.result(frontsResult, 1.second)
