package com.ruchij.api.web

import cats.effect.kernel.Async
import com.ruchij.api.services.applicationhealth.ApplicationHealthService
import com.ruchij.api.services.user.UserService
import com.ruchij.api.web.middleware.{ExceptionHandler, NotFoundHandler}
import com.ruchij.api.web.routes.{HealthRoutes, UserRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.middleware.GZip
import org.http4s.{HttpApp, HttpRoutes}

object Routes {
  def apply[F[_]: Async](userService: UserService[F], applicationHealthService: ApplicationHealthService[F]): HttpApp[F] = {
    given Http4sDsl[F] = new Http4sDsl[F] {}

    val routes: HttpRoutes[F] =
      Router(
        "/user" -> UserRoutes(userService),
        "/service" -> HealthRoutes(applicationHealthService)
      )

    GZip {
      ExceptionHandler {
        NotFoundHandler(routes)
      }
    }
  }
}
