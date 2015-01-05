package lib

import com.amazonaws.services.s3.AmazonS3Client
import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}

private case class TestContentApiClient(override val apiKey: String, override val targetUrl: String) extends GuardianContentClient(apiKey)

trait IntegrationTestConfig extends ExecutionContext {
  val apiKey: String = scala.sys.env.getOrElse("CONTENT_API_KEY", "")
  val targetUrl: Option[String] = scala.sys.env.get("FACIA_CLIENT_TARGET_URL")

  implicit val capiClient: GuardianContentClient =
    targetUrl.fold(ifEmpty = new GuardianContentClient(apiKey)){ targetUrl =>
      new TestContentApiClient(
        apiKey,
        targetUrl)}

  private val amazonS3Client = new AmazonS3Client()
  implicit val apiClient: ApiClient = ApiClient("aws-frontend-store", "DEV", AmazonSdkS3Client(amazonS3Client))
}
