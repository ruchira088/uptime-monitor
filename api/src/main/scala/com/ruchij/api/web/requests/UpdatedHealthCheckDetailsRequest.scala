package com.ruchij.api.web.requests

import org.http4s.Uri
import org.http4s.Method

final case class UpdatedHealthCheckDetailsRequest(
  name: Option[String],
  description: Option[String]
)