package com.ruchij.api.web.requests

import com.ruchij.api.web.requests.CreateHealthCheckRequest.HttpHeaderRequest
import org.http4s.{Header, Method, Uri}
import org.typelevel.ci.CIString

final case class CreateHealthCheckRequest(
  name: String,
  description: Option[String],
  method: Method,
  url: Uri,
  httpHeaders: Option[List[HttpHeaderRequest]],
  body: Option[String]
) {
  val headers: List[Header.Raw] =
    httpHeaders.toList.flatten.map(httpHeader => Header.Raw(CIString(httpHeader.name), httpHeader.value))
}

object CreateHealthCheckRequest {
  final case class HttpHeaderRequest(name: String, value: String)
}
