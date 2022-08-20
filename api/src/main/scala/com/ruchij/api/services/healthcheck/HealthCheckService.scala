package com.ruchij.api.services.healthcheck

import org.http4s.Method
import org.http4s.Uri
import org.http4s.Header
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.dao.healthcheck.models.HealthCheckDetails
import com.ruchij.api.services.healthcheck.models.HealthCheck

trait HealthCheckService[F[_]] {
  def create(
    userId: ID[User],
    name: String,
    description: Option[String],
    method: Method,
    url: Uri,
    headers: List[Header.Raw],
    body: Option[Seq[Byte]]
  ): F[HealthCheck]

  def getById(userId: ID[User], id: ID[HealthCheckDetails]): F[HealthCheck]
}
