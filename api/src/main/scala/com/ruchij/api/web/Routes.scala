package com.ruchij.api.web

import com.ruchij.api.services.health.HealthService
import com.ruchij.api.services.user.UserService
import com.ruchij.api.web.middleware.{ExceptionHandler, NotFoundHandler}
import com.ruchij.api.web.routes.HealthRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.middleware.GZip
import org.http4s.{HttpApp, HttpRoutes}
import com.ruchij.api.web.routes.UserRoutes
import cats.effect.kernel.Async

object Routes {
  def apply[F[_]: Async](userService: UserService[F], healthService: HealthService[F]): HttpApp[F] = {
    given Http4sDsl[F] = new Http4sDsl[F] {}

    val routes: HttpRoutes[F] =
      Router(
        "/user" -> UserRoutes(userService),
        "/service" -> HealthRoutes(healthService)
      )

    GZip {
      ExceptionHandler {
        NotFoundHandler(routes)
      }
    }
  }
}
