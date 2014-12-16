package lib

import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.config.FaciaConfig

trait IntegrationTestConfig extends ExecutionContext {
  implicit val capiClient = new GuardianContentClient(apiKey = "test")
  implicit val dispatchClient = dispatch.Http
  implicit object FaciaConfig extends FaciaConfig {
    override def stage: String = "PROD"
    override def bucket: String = "aws-frontend-store"
    override def accessKey: String = ""  // don't need these - the json is public at the moment
    override def secretKey: String = ""
  }
}
