package com.ruchij.api.web.requests

import com.ruchij.api.web.requests.CreateHealthCheckRequest.HttpHeader
import org.http4s.{Header, Method, Uri}
import org.typelevel.ci.CIString

final case class CreateHealthCheckRequest(
  name: String,
  description: Option[String],
  method: Method,
  url: Uri,
  httpHeaders: List[HttpHeader]
) {
  val headers: List[Header.Raw] =
    httpHeaders.map(httpHeader => Header.Raw(CIString(httpHeader.name), httpHeader.value))
}

object CreateHealthCheckRequest {
  final case class HttpHeader(name: String, value: String)
}
