package com.gu.facia.client

case class HttpResponse(
  statusCode: Int,
  statusLine: String,
  body: String
)

trait Http {
  def get(url: String): HttpResponse
}
