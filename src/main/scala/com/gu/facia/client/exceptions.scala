package com.gu.facia.client

case class HttpError(url: Url, statusCode: Int, statusText: String, body: String)
  extends RuntimeException(s"$statusCode error accessing ${url.url}: $statusText")

case class JsonDeserialisationError(errorMessage: String) extends RuntimeException(errorMessage)