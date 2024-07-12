package lib

//import com.amazonaws.auth._
import software.amazon.awssdk.auth.credentials.{EnvironmentVariableCredentialsProvider, ProfileCredentialsProvider}
// import com.amazonaws.auth.profile.ProfileCredentialsProvider
//import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.gu.contentapi.client.GuardianContentClient
import com.gu.etagcaching.aws.sdkv2.s3.S3ObjectFetching
import com.gu.etagcaching.aws.sdkv2.s3.response.Transformer.Bytes
import com.gu.facia.client.{ApiClient, Environment}
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, AwsCredentialsProviderChain}
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
      TestContentApiClient(apiKey, targetUrl)
    }
  }

  def credentialsForDevAndCI(devProfile: String, ciCreds: AwsCredentialsProvider): AwsCredentialsProviderChain =
    AwsCredentialsProviderChain.of(ciCreds, ProfileCredentialsProvider.builder().profileName(devProfile).build())

  implicit val apiClient: ApiClient = ApiClient.withCaching(
    "facia-tool-store",
    Environment.Dev,
    S3ObjectFetching(S3AsyncClient.builder()
      .region(Region.EU_WEST_1)
      .credentialsProvider(credentialsForDevAndCI(awsProfileName, EnvironmentVariableCredentialsProvider.create())).build(), Bytes).mapResponse(_.asByteArray())
  )
}
