import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider, SystemPropertiesCredentialsProvider}
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.FAPI
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}

import scala.concurrent.Await
import scala.concurrent.duration._

implicit val executionContext = scala.concurrent.ExecutionContext.global

// demo config
val apiKey = "INSERT_CONTENT_API_KEY_HERE"
val awsProfileName = "cmsFronts"

implicit val capiClient =  new GuardianContentClient(apiKey)
implicit val apiClient: ApiClient = {
  val amazonS3Client = {
    val credentialsProvider = new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new SystemPropertiesCredentialsProvider(),
      new ProfileCredentialsProvider(awsProfileName)
    )
    new AmazonS3Client(credentialsProvider)
  }
  ApiClient("aws-frontend-store", "DEV", AmazonSdkS3Client(amazonS3Client))
}

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
