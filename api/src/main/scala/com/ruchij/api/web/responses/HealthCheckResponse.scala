package com.ruchij.api.web.responses

import com.ruchij.api.dao.healthcheck.models.HealthCheckDetails
import com.ruchij.api.dao.http.models.{HttpEndpoint, HttpHeader, HttpRequestBody}
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.services.healthcheck.models.HealthCheck
import com.ruchij.api.web.requests.CreateHealthCheckRequest.HttpHeaderRequest
import com.ruchij.api.web.responses.HealthCheckResponse.{HealthCheckDetailsResponse, HttpEndpointResponse, HttpHeaderResponse, HttpRequestBodyResponse}
import org.http4s.headers.`Content-Type`
import org.http4s.{Method, Uri}

final case class HealthCheckResponse(
  healthCheckDetails: HealthCheckDetailsResponse,
  httpEndpoint: HttpEndpointResponse,
  headers: Seq[HttpHeaderResponse],
  body: Option[HttpRequestBodyResponse]
)

object HealthCheckResponse {
  final case class HealthCheckDetailsResponse(id: ID[HealthCheckDetails], name: String, description: Option[String])
  final case class HttpEndpointResponse(id: ID[HttpEndpoint], method: Method, url: Uri)
  final case class HttpHeaderResponse(id: ID[HttpHeader], name: String, value: String)
  final case class HttpRequestBodyResponse(id: ID[HttpRequestBody], contentType: `Content-Type`, body: String)

  def from(healthCheck: HealthCheck): HealthCheckResponse =
    HealthCheckResponse(
      from(healthCheck.healthCheckDetails),
      from(healthCheck.httpEndpoint),
      healthCheck.headers.map(header => from(header)),
      healthCheck.body.map(body => from(body))
    )

  private def from(healthCheckDetails: HealthCheckDetails): HealthCheckDetailsResponse =
    HealthCheckDetailsResponse(
      healthCheckDetails.id,
      healthCheckDetails.name,
      healthCheckDetails.description
    )

  private def from(httpEndpoint: HttpEndpoint): HttpEndpointResponse =
    HttpEndpointResponse(httpEndpoint.id, httpEndpoint.method, httpEndpoint.url)

  private def from(httpHeader: HttpHeader): HttpHeaderResponse =
    HttpHeaderResponse(httpHeader.id, httpHeader.name, httpHeader.value)

  private def from(httpRequestBody: HttpRequestBody): HttpRequestBodyResponse =
    HttpRequestBodyResponse(httpRequestBody.id, httpRequestBody.contentType, httpRequestBody.body)

}
