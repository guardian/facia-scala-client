package com.gu.facia.api.http

import com.fasterxml.jackson.core.JsonParseException
import com.gu.facia.api.Response.Async._
import com.ning.http.client.Request
import dispatch.FunctionHandler
import play.api.libs.json.JsValue
import play.api.libs.json.Json.parse
import com.gu.facia.api.{HttpError, JsonError, Response}

import scala.concurrent.ExecutionContext


@deprecated
case class HttpResponse(body: String, statusCode: Int, statusMessage: String)

@deprecated
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
      json <- Json.toJson(okResponse.body)
    } yield json
  }
}

@deprecated
object Json {

  import com.gu.facia.api.Response.{Left, Right}

  def toJson(string: String): Response[JsValue] = {
     try {
       Right(parse(string))
     } catch {
       case e: JsonParseException =>
         Left(JsonError("Error parsing response JSON", Some(e)))
     }
  }
}
