package com.ruchij.api.dao.http.models

import com.ruchij.api.dao.models.IDs.ID
import org.joda.time.DateTime
import com.ruchij.api.dao.http.HttpEndpoint

final case class HttpHeader(
  id: ID[HttpHeader],
  createdAt: DateTime,
  httpEndpointId: ID[HttpEndpoint],
  name: String,
  value: String
)
