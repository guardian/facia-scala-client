package com.gu.facia.client

import scala.concurrent.{ExecutionContext, Future}
import com.gu.facia.client.models.{Collection, Config}
import play.api.libs.json.{Reads, Json}

case class ApiClient(root: Url, http: Http)(implicit executionContext: ExecutionContext) {
  private def retrieve[A: Reads](url: Url) = http.get(url) map { response: HttpResponse =>
    if (response.statusCode == 200) {
      Json.fromJson[A](Json.parse(response.body)) getOrElse {
        throw new JsonDeserialisationError(s"Could not deserialize JSON at $url")
      }
    } else {
      throw new HttpError(url, response.statusCode, response.statusLine, response.body)
    }
  }

  def config: Future[Config] =
    retrieve[Config](root / "frontsapi" / "config" / "config.json")

  def collection(id: String): Future[Collection] =
    retrieve[Collection](root / "frontsapi" / "collection" / id / "collection.json")
}
