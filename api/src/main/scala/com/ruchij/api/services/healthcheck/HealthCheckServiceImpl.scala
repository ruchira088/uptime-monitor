package com.ruchij.api.services.healthcheck

import com.ruchij.api.dao.models.IDs.ID
import org.http4s.Method
import org.http4s.Header.Raw
import org.http4s.Uri
import cats.~>
import cats.implicits.*
import com.ruchij.api.dao.healthcheck.models.HealthCheckDetails
import com.ruchij.api.dao.healthcheck.HealthCheckDetailsDao
import com.ruchij.api.services.healthcheck.models.HealthCheck
import com.ruchij.api.dao.user.models.User
import cats.effect.kernel.Sync
import com.ruchij.api.types.JodaClock
import com.ruchij.api.dao.http.HttpEndpointDao
import com.ruchij.api.dao.http.HttpHeaderDao
import com.ruchij.api.dao.http.HttpRequestBodyDao
import cats.Monad

class HealthCheckServiceImpl[F[_]: Sync: JodaClock, G[_]: Monad](
  healthCheckDetailsDao: HealthCheckDetailsDao[G],
  httpEndpointDao: HttpEndpointDao[G],
  httpHeaderDao: HttpHeaderDao[G],
  httpRequestBodyDao: HttpRequestBodyDao[G]
)(using transaction: G ~> F)
    extends HealthCheckService[F] {
  
  override def create(
    userId: ID[User],
    name: String,
    description: Option[String],
    method: Method,
    url: Uri,
    headers: List[Raw],
    body: Option[Seq[Byte]]
  ): F[HealthCheck] = ???

  override def getById(userId: ID[User], id: ID[HealthCheckDetails]): F[HealthCheck] = ???
}
