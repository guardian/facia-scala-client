package lib

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}

private case class TestContentApiClient(override val apiKey: String, override val targetUrl: String)
  extends GuardianContentClient(apiKey)

trait IntegrationTestConfig extends ExecutionContext {

  private val apiKey: String = scala.sys.env.getOrElse("CONTENT_API_KEY", "")
  private val targetUrl: Option[String] = scala.sys.env.get("FACIA_CLIENT_TARGET_URL")
  private val awsProfileName: Option[String] = scala.sys.env.get("AWS_PROFILE_NAME")

  implicit val capiClient: GuardianContentClient = {
    targetUrl.fold(ifEmpty = new GuardianContentClient(apiKey)) { targetUrl =>
      new TestContentApiClient(apiKey, targetUrl)
    }
  }

  implicit val apiClient: ApiClient = {
    val credentialsProvider = new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new SystemPropertiesCredentialsProvider(),
      awsProfileName map {
        new ProfileCredentialsProvider(_)
      } getOrElse new ProfileCredentialsProvider()
    )
    val amazonS3Client = new AmazonS3Client(credentialsProvider)
    ApiClient("aws-frontend-store", "DEV", AmazonSdkS3Client(amazonS3Client))
  }
}
