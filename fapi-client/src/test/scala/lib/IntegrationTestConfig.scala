package lib

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}
import com.amazonaws.regions.Regions.EU_WEST_1

private case class TestContentApiClient(override val apiKey: String, override val targetUrl: String)
  extends GuardianContentClient(apiKey)

trait IntegrationTestConfig extends ExecutionContext {

  private val apiKey: String = scala.sys.env.getOrElse("CONTENT_API_KEY", "")
  private val targetUrl: Option[String] = scala.sys.env.get("FACIA_CLIENT_TARGET_URL")
  private val awsProfileName: String = "cmsFronts"

  implicit val capiClient: GuardianContentClient = {
    targetUrl.fold(ifEmpty = new GuardianContentClient(apiKey)) { targetUrl =>
      TestContentApiClient(apiKey, targetUrl)
    }
  }

  implicit val apiClient: ApiClient = {
    val credentialsProvider = new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new SystemPropertiesCredentialsProvider(),
      new ProfileCredentialsProvider(awsProfileName)
    )
    val amazonS3Client = AmazonS3ClientBuilder.standard()
      .withRegion(EU_WEST_1)
      .withCredentials(credentialsProvider)
      .build()
    ApiClient("facia-tool-store", "DEV", AmazonSdkS3Client(amazonS3Client))
  }
}
