package com.ruchij.api.dao.http.models

import com.ruchij.api.dao.models.IDs.ID
import org.joda.time.DateTime
import com.ruchij.api.dao.http.models.HttpEndpoint
import org.http4s.headers.`Content-Type`

final case class HttpRequestBody(
  id: ID[HttpRequestBody],
  createdAt: DateTime,
  httpEndpointId: ID[HttpEndpoint],
  contentType: `Content-Type`,
  body: String
)
