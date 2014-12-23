package lib

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}

trait IntegrationTestConfig extends ExecutionContext {
  implicit val capiClient = new GuardianContentClient(apiKey = "test")

  private val amazonS3Client = new AmazonS3Client(new BasicAWSCredentials("key", "secret-key"))
  implicit val apiClient: ApiClient = ApiClient("aws-frontend-store", "DEV", AmazonSdkS3Client(amazonS3Client))
}
