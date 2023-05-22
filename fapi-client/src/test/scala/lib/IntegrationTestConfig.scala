package lib

import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain, EnvironmentVariableCredentialsProvider, ProfileCredentialsProvider, SystemPropertyCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient

private case class TestContentApiClient(override val apiKey: String, override val targetUrl: String)
  extends GuardianContentClient(apiKey)

trait IntegrationTestConfig extends ExecutionContext {

  private val apiKey: String = scala.sys.env.getOrElse("CONTENT_API_KEY", "")
  private val targetUrl: Option[String] = scala.sys.env.get("FACIA_CLIENT_TARGET_URL")
  private val awsProfileName: String = "cmsFronts"

  implicit val capiClient: GuardianContentClient = {
    targetUrl.fold(ifEmpty = new GuardianContentClient(apiKey)) { targetUrl =>
      new TestContentApiClient(apiKey, targetUrl)
    }
  }

  implicit val apiClient: ApiClient = {
    val credentials = AwsCredentialsProviderChain.builder()
      .credentialsProviders(
        EnvironmentVariableCredentialsProvider.create(),
        SystemPropertyCredentialsProvider.create(),
        ProfileCredentialsProvider.create(awsProfileName)
      )
      .build()

    val amazonS3Client = S3AsyncClient.builder()
      .region(Region.EU_WEST_1)
      .credentialsProvider(credentials)
      .build()

    ApiClient("facia-tool-store", "TEST", AmazonSdkS3Client(amazonS3Client))
  }
}
