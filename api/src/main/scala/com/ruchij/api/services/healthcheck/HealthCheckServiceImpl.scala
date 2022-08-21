package com.ruchij.api.services.healthcheck

import com.ruchij.api.dao.models.IDs.ID
import org.http4s.Method
import org.http4s.Header
import org.http4s.Uri
import cats.~>
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
import cats.Monad
import com.ruchij.api.types.IdGenerator
import org.http4s.syntax.header
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType
import doobie.util.transactor
import com.ruchij.api.types.Logger

class HealthCheckServiceImpl[F[_]: Sync: JodaClock: IdGenerator, G[_]: Monad](
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
    body: Option[Seq[Byte]]
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
        HttpEndpoint(
          id = httpEndpointId, 
          createdAt = timestamp, 
          updatedAt = timestamp,
          method = method,
          url = url
        )

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
        body.map {data => 
          IdGenerator[F].generate[HttpRequestBody]
            .map { httpRequestBodyId =>
              val contentType = 
                headers.find(header => Header[`Content-Type`].name == header.name)
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
        }
        .sequence

      insertions = 
        List(httpEndpointDao.insert(httpEndpoint)) ++
            httpHeaders.map(httpHeaderDao.insert) ++
            httpRequestBody.map(httpRequestBodyDao.insert).toList

      entries <- transaction(insertions.traverse(identity)).map(_.sum)

      healthCheck =
        HealthCheck(
          healthCheckDetails,
          httpEndpoint,
          httpHeaders,
          httpRequestBody
        )
    }
    yield healthCheck

  override def getById(userId: ID[User], id: ID[HealthCheckDetails]): F[HealthCheck] = ???
}
