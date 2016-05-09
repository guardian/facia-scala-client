import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider, SystemPropertiesCredentialsProvider}
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.FAPI
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}

import scala.concurrent.Await
import scala.concurrent.duration._

// demo config
implicit val capiClient = {
  val apiKey: String = scala.sys.env.getOrElse("CONTENT_API_KEY", "")
  new GuardianContentClient(apiKey)
}
private val amazonS3Client = {
  val awsProfileName = scala.sys.env.get("AWS_PROFILE_NAME")
  val credentialsProvider = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider(),
    new SystemPropertiesCredentialsProvider(),
    new ProfileCredentialsProvider(awsProfileName.getOrElse(""))
  )
  new AmazonS3Client(credentialsProvider)
}
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
