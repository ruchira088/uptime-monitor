package com.ruchij.api.web.routes

import cats.effect.Async
import cats.implicits.*
import com.ruchij.api.circe.Decoders.given
import com.ruchij.api.circe.Encoders.given
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.services.authentication.AuthenticationService
import com.ruchij.api.services.healthcheck.HealthCheckService
import com.ruchij.api.web.middleware.UserAuthenticator
import com.ruchij.api.web.requests.CreateHealthCheckRequest
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
                body = None
              )

            response <- Created(healthCheck)
          }
          yield response
      }
    }
  }
}
