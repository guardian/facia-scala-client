package lib

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}

private case class TestContentApiClient(override val apiKey: String, override val targetUrl: String)
  extends GuardianContentClient(apiKey)

trait IntegrationTestConfig extends ExecutionContext {

  implicit val capiClient: GuardianContentClient = {
    val apiKey: String = scala.sys.env.getOrElse("CONTENT_API_KEY", "")
    val targetUrl: Option[String] = scala.sys.env.get("FACIA_CLIENT_TARGET_URL")
    targetUrl.fold(ifEmpty = new GuardianContentClient(apiKey)) { targetUrl =>
      new TestContentApiClient(apiKey, targetUrl)
    }
  }

  implicit val apiClient: ApiClient = {
    val awsProfileName: Option[String] = scala.sys.env.get("AWS_PROFILE_NAME")
    val credentialsProvider = new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new SystemPropertiesCredentialsProvider(),
      new ProfileCredentialsProvider(awsProfileName.getOrElse(""))
    )
    val amazonS3Client = new AmazonS3Client(credentialsProvider)
    ApiClient("aws-frontend-store", "DEV", AmazonSdkS3Client(amazonS3Client))
  }
}
