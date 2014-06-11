package com.gu.facia.client

import scala.concurrent.Future

case class HttpResponse(
  statusCode: Int,
  statusLine: String,
  body: String
)

trait Http {
  def get(url: Url): Future[HttpResponse]
}
