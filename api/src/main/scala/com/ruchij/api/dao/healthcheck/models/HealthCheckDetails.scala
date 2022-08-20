package com.ruchij.api.dao.healthcheck.models

import com.ruchij.api.dao.models.IDs.ID
import org.joda.time.DateTime
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.dao.http.models.HttpEndpoint

final case class HealthCheckDetails(
  id: ID[HealthCheckDetails],
  createdAt: DateTime,
  name: String,
  description: Option[String],
  userId: ID[User],
  httpEndpointId: ID[HttpEndpoint]
)