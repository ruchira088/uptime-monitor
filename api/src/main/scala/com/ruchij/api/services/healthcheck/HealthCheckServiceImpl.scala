package com.ruchij.api.services.healthcheck

import cats.data.OptionT
import com.ruchij.api.dao.models.IDs.ID
import org.http4s.Method
import org.http4s.Header
import org.http4s.Uri
import cats.{Monad, MonadThrow, ~>}
import cats.implicits.*
import com.ruchij.api.dao.healthcheck.models.HealthCheckDetails
import com.ruchij.api.dao.healthcheck.HealthCheckDetailsDao
import com.ruchij.api.services.healthcheck.models.HealthCheck
import com.ruchij.api.dao.user.models.User
import cats.effect.kernel.Sync
import com.ruchij.api.types.JodaClock
import com.ruchij.api.dao.http.models.HttpEndpoint
import com.ruchij.api.dao.http.models.HttpHeader
import com.ruchij.api.dao.http.models.HttpRequestBody
import com.ruchij.api.dao.http.HttpEndpointDao
import com.ruchij.api.dao.http.HttpHeaderDao
import com.ruchij.api.dao.http.HttpRequestBodyDao
import com.ruchij.api.types.IdGenerator
import org.http4s.syntax.header
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType
import doobie.util.transactor
import com.ruchij.api.types.Logger
import com.ruchij.api.exceptions.ResourceNotFoundException.apply
import com.ruchij.api.exceptions.ResourceNotFoundException

class HealthCheckServiceImpl[F[_]: Sync: JodaClock: IdGenerator, G[_]: MonadThrow](
  healthCheckDetailsDao: HealthCheckDetailsDao[G],
  httpEndpointDao: HttpEndpointDao[G],
  httpHeaderDao: HttpHeaderDao[G],
  httpRequestBodyDao: HttpRequestBodyDao[G]
)(using transaction: G ~> F)
    extends HealthCheckService[F] {

  private val logger = Logger[HealthCheckServiceImpl[F, G]]

  override def create(
    userId: ID[User],
    name: String,
    description: Option[String],
    method: Method,
    url: Uri,
    headers: List[Header.Raw],
    maybeBody: Option[String]
  ): F[HealthCheck] =
    for {
      timestamp <- JodaClock[F].timestamp
      healthCheckId <- IdGenerator[F].generate[HealthCheckDetails]
      httpEndpointId <- IdGenerator[F].generate[HttpEndpoint]

      healthCheckDetails =
        HealthCheckDetails(
          id = healthCheckId,
          createdAt = timestamp,
          name = name,
          description = description,
          userId = userId,
          httpEndpointId = httpEndpointId
        )

      httpEndpoint =
        HttpEndpoint(id = httpEndpointId, createdAt = timestamp, updatedAt = timestamp, method = method, url = url)

      httpHeaders <-
        headers.traverse { header =>
          IdGenerator[F].generate[HttpHeader].map { httpHeaderId =>
            HttpHeader(
              id = httpHeaderId,
              createdAt = timestamp,
              httpEndpointId = httpEndpointId,
              name = header.name.toString,
              value = header.value
            )
          }
        }

      httpRequestBody <-
        maybeBody.map { data =>
          IdGenerator[F]
            .generate[HttpRequestBody]
            .map { httpRequestBodyId =>
              val contentType =
                headers
                  .find(header => Header[`Content-Type`].name == header.name)
                  .flatMap(header => `Content-Type`.parse(header.value).toOption)
                  .getOrElse(`Content-Type`(MediaType.text.plain))

              HttpRequestBody(
                id = httpRequestBodyId,
                createdAt = timestamp,
                httpEndpointId = httpEndpointId,
                contentType = contentType,
                body = data
              )
            }
        }.sequence

      insertions =
        List(httpEndpointDao.insert(httpEndpoint)) ++
          httpHeaders.map(httpHeaderDao.insert) ++
          httpRequestBody.map(httpRequestBodyDao.insert).toList

      entries <- transaction(insertions.traverse(identity)).map(_.sum)

      healthCheck =
        HealthCheck(healthCheckDetails, httpEndpoint, httpHeaders, httpRequestBody)
    } yield healthCheck

  override def getById(id: ID[HealthCheckDetails], maybeUserId: Option[ID[User]]): F[HealthCheck] =
    transaction {
      OptionT(healthCheckDetailsDao.findById(id, maybeUserId))
        .flatMap(getHealthCheck)
        .getOrRaise(ResourceNotFoundException(s"HealthCheck not found id=$id"))
    }

  override def getByUserId(userId: ID[User]): F[Seq[HealthCheck]] =
    transaction {
      healthCheckDetailsDao.findByUserId(userId)
        .flatMap {
          _.traverse { healthCheckDetails => getHealthCheck(healthCheckDetails).value }
        }
        .map {
          _.flatMap(_.toSeq)
        }
    }

  private def getHealthCheck(healthCheckDetails: HealthCheckDetails): OptionT[G, HealthCheck] =
    for {
      httpEndpoint <- OptionT(httpEndpointDao.findById(healthCheckDetails.httpEndpointId))
      headers <- OptionT.liftF(httpHeaderDao.findByHttpEndpointId(httpEndpoint.id))
      maybeRequestBody <- OptionT.liftF(httpRequestBodyDao.findByHttpEndpointId(httpEndpoint.id))
    }
    yield HealthCheck(healthCheckDetails, httpEndpoint, headers, maybeRequestBody)
}
