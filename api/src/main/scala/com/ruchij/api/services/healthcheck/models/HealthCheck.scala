package com.ruchij.api.services.healthcheck.models

import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.healthcheck.models.HealthCheckDetails
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.dao.http.models.HttpEndpoint
import com.ruchij.api.dao.http.models.HttpHeader
import com.ruchij.api.dao.http.models.HttpRequestBody

final case class HealthCheck(
  healthCheckDetails: HealthCheckDetails,
  httpEndpoint: HttpEndpoint,
  headers: Seq[HttpHeader],
  body: Option[HttpRequestBody]
)