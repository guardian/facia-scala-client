package com.gu.facia.api.http

import com.gu.facia.api.Response.Async._
import play.api.libs.json.Json
//import com.gu.facia.api.json.Json
import com.gu.facia.api.{HttpError, Response}
import com.ning.http.client.Request
import dispatch.FunctionHandler
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext


case class HttpResponse(body: String, statusCode: Int, statusMessage: String)
object HttpResponse {
  val dispatchHandler = new FunctionHandler({ response =>
    HttpResponse(response.getResponseBody("utf-8"), response.getStatusCode, response.getStatusText)
  })

  def okToRight(response: HttpResponse): Response[HttpResponse] = {
    if (response.statusCode < 400) Response.Right(response)
    else Response.Left(HttpError(s"Request failed with status code ${response.statusCode}"))
  }

  def jsonResponse(request: Request, client: dispatch.Http)(implicit executionContext: ExecutionContext): Response[JsValue] = {
    for {
      rawResponse <- Right(client(request, HttpResponse.dispatchHandler))
      okResponse <- HttpResponse.okToRight(rawResponse)
    } yield Json.toJson(okResponse.body)
  }
}
