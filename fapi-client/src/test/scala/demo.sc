import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.FAPI
import com.gu.facia.api.config.FaciaConfig
import concurrent.duration._

import scala.concurrent.Await

// demo config
implicit val capiClient = new GuardianContentClient(apiKey = "test")
implicit val dispatchClient = dispatch.Http
implicit val executionContext = scala.concurrent.ExecutionContext.global
implicit object FaciaConfig extends FaciaConfig {
  override def stage: String = "PROD"
  override def bucket: String = "aws-frontend-store"
  override def accessKey: String = ""  // don't need these - the json is public at the moment
  override def secretKey: String = ""
}

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
