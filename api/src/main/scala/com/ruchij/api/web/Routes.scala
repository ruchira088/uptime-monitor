package com.ruchij.api.web

import cats.effect.kernel.Async
import com.ruchij.api.services.applicationhealth.ApplicationHealthService
import com.ruchij.api.services.authentication.AuthenticationService
import com.ruchij.api.services.healthcheck.HealthCheckService
import com.ruchij.api.services.user.UserService
import com.ruchij.api.web.middleware.ExceptionHandler
import com.ruchij.api.web.middleware.NotFoundHandler
import com.ruchij.api.web.routes.AuthenticationRoutes
import com.ruchij.api.web.routes.HealthCheckRoutes
import com.ruchij.api.web.routes.ServiceRoutes
import com.ruchij.api.web.routes.UserRoutes
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.GZip

object Routes {
  def apply[F[_]: Async](
    userService: UserService[F],
    authenticationService: AuthenticationService[F],
    healthCheckService: HealthCheckService[F],
    applicationHealthService: ApplicationHealthService[F]
  ): HttpApp[F] = {
    given Http4sDsl[F] = new Http4sDsl[F] {}

    val routes: HttpRoutes[F] =
      Router(
        "/user" -> UserRoutes(userService),
        "/authentication" -> AuthenticationRoutes(authenticationService),
        "/health-check" -> HealthCheckRoutes(healthCheckService, authenticationService),
        "/service" -> ServiceRoutes(applicationHealthService)
      )

    val cors = 
      CORS.policy
        .withAllowCredentials(true)
        .withAllowOriginHeader(_ => true)

    GZip {
      cors {
        ExceptionHandler {
          NotFoundHandler(routes)
        }
      }
    }
  }
}
