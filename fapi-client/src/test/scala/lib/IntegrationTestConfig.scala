package lib

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.config.FaciaConfig
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}

trait IntegrationTestConfig extends ExecutionContext {
  implicit val capiClient = new GuardianContentClient(apiKey = "test")

  private val amazonS3Client = new AmazonS3Client(new BasicAWSCredentials("key", "secret-key"))
  implicit val apiClient: ApiClient = ApiClient("aws-frontend-store", "DEV", AmazonSdkS3Client(amazonS3Client))

  implicit object FaciaConfig extends FaciaConfig {
    override def stage: String = "PROD"
    override def bucket: String = "aws-frontend-store"
    override def accessKey: String = ""  // don't need these - the json is public at the moment
    override def secretKey: String = ""
  }
}
