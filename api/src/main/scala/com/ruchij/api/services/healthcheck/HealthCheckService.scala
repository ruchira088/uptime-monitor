package com.ruchij.api.services.healthcheck

import org.http4s.Method
import org.http4s.Uri
import org.http4s.Header
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.dao.healthcheck.models.HealthCheckDetails
import com.ruchij.api.services.healthcheck.models.HealthCheck
import com.ruchij.api.dao.http.models.HttpHeader

trait HealthCheckService[F[_]] {
  def create(
    userId: ID[User],
    name: String,
    description: Option[String],
    method: Method,
    url: Uri,
    headers: List[Header.Raw],
    maybeBody: Option[String]
  ): F[HealthCheck]

  def getById(id: ID[HealthCheckDetails], maybeUserId: Option[ID[User]]): F[HealthCheck]

  def getByUserId(userId: ID[User]): F[Seq[HealthCheck]]

  def updateHealthCheckDetails(
    id: ID[HealthCheckDetails],
    maybeName: Option[String],
    maybeDescription: Option[String],
    maybeUserId: Option[ID[User]]
  ): F[HealthCheckDetails]

  def updateHeader(
    headerId: ID[HttpHeader],
    maybeKey: Option[String],
    maybeValue: Option[String],
    maybeUserId: Option[ID[User]]
  ): F[HttpHeader]
}
