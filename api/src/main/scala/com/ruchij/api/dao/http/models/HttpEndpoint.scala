package com.ruchij.api.dao.http

import com.ruchij.api.dao.models.IDs.ID
import org.joda.time.DateTime
import org.http4s.Method
import org.http4s.Uri

final case class HttpEndpoint(id: ID[HttpEndpoint], createdAt: DateTime, updatedAt: DateTime, method: Method, url: Uri)
