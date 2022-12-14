package com.ruchij.api.web.routes

import cats.effect.Sync
import cats.implicits.toFlatMapOps
import com.ruchij.api.circe.Encoders.given
import com.ruchij.api.services.applicationhealth.ApplicationHealthService
import io.circe.generic.auto.deriveEncoder
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.Http4sDsl

object ServiceRoutes {
  def apply[F[_]: Sync](applicationHealthService: ApplicationHealthService[F])(using dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "info" =>
        applicationHealthService.serviceInformation
          .flatMap(serviceInformation => Ok(serviceInformation))

      case GET -> Root / "health" =>
        applicationHealthService.healthCheck
          .flatMap {
            serviceHealthStatus => 
              if serviceHealthStatus.isHealthy 
              then Ok(serviceHealthStatus) else ServiceUnavailable(serviceHealthStatus)
          }
    }
  }
}
