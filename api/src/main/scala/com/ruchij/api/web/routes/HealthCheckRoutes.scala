package com.ruchij.api.web.routes

import cats.effect.Async
import cats.implicits.*
import com.ruchij.api.circe.Decoders.given
import com.ruchij.api.circe.Encoders.given
import com.ruchij.api.dao.healthcheck.models.HealthCheckDetails
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.services.authentication.AuthenticationService
import com.ruchij.api.services.healthcheck.HealthCheckService
import com.ruchij.api.services.healthcheck.models.HealthCheck
import com.ruchij.api.web.middleware.UserAuthenticator
import com.ruchij.api.web.requests.CreateHealthCheckRequest
import com.ruchij.api.web.requests.UpdateHealthCheckDetailsRequest
import com.ruchij.api.web.requests.UpdateHeaderRequest
import com.ruchij.api.types.FunctionKTypes.{*, given}
import com.ruchij.api.web.responses.HealthCheckResponse
import com.ruchij.api.dao.http.models.HttpHeader
import org.http4s.{ContextRoutes, HttpRoutes}
import io.circe.generic.auto.*
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec.*

object HealthCheckRoutes {
  def apply[F[_]: Async](healthCheckService: HealthCheckService[F], authenticationService: AuthenticationService[F])(
    using dsl: Http4sDsl[F]
  ): HttpRoutes[F] = {
    import dsl._

    UserAuthenticator(authenticationService).apply {
      ContextRoutes.of[User, F] {
        case authenticatedRequest @ POST -> Root as user =>
          for {
            createHealthCheckRequest <- authenticatedRequest.req.as[CreateHealthCheckRequest]

            healthCheck <-
              healthCheckService.create(
                userId = user.id,
                name = createHealthCheckRequest.name,
                description = createHealthCheckRequest.description,
                method = createHealthCheckRequest.method,
                url = createHealthCheckRequest.url,
                headers = createHealthCheckRequest.headers,
                maybeBody = createHealthCheckRequest.body
              )

            response <- Created(HealthCheckResponse.from(healthCheck))
          }
          yield response

        case GET -> Root / "id" / id as user =>
          for {
            healthCheckDetailsId <- ID.parse[HealthCheckDetails](id).toType[F, Throwable]
            healthCheck <- healthCheckService.getById(healthCheckDetailsId, Some(user.id))
            response <- Ok(healthCheck)
          }
          yield response

        case authenticatedRequest @ PATCH -> Root / "details" / id as user =>
          for {
            healthCheckDetailsId <- ID.parse[HealthCheckDetails](id).toType[F, Throwable]
            updatedHealthCheckDetailsRequest <- authenticatedRequest.req.as[UpdateHealthCheckDetailsRequest]
            healthCheckDetails <- 
              healthCheckService.updateHealthCheckDetails(
                healthCheckDetailsId, 
                updatedHealthCheckDetailsRequest.name, 
                updatedHealthCheckDetailsRequest.description, 
                Some(user.id)
              )
            
            response <- Ok(healthCheckDetails)
          }
          yield response

        case authenticatedRequest @ PATCH -> Root / "headers" / id as user =>
          for {
            httpHeaderId <- ID.parse[HttpHeader](id).toType[F, Throwable]
            updateHeaderRequest <- authenticatedRequest.req.as[UpdateHeaderRequest]
            httpHeader <- 
              healthCheckService.updateHeader(
                httpHeaderId, 
                updateHeaderRequest.name, 
                updateHeaderRequest.value,
                Some(user.id)
              )

            response <- Ok(httpHeader)
          }
          yield response
      }
    }
  }
}
