package com.ruchij.api.services.health

import cats.effect.{Async, Clock, Concurrent, Sync}
import cats.implicits.*
import cats.~>
import com.ruchij.api.config.BuildInformation
import com.ruchij.api.services.health.models.{HealthCheck, HealthStatus, ServiceInformation}
import com.ruchij.api.types.JodaClock
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits.uri
import org.http4s.Status
import cats.Applicative.apply
import cats.Applicative

class HealthServiceImpl[F[_]: JodaClock: Async](client: Client[F], buildInformation: BuildInformation)(using
  transaction: ConnectionIO ~> F
) extends HealthService[F] {
  private val http4sClientDsl = new Http4sClientDsl[F] {}

  import http4sClientDsl._

  override val serviceInformation: F[ServiceInformation] =
    JodaClock[F].timestamp
      .flatMap(timestamp => ServiceInformation.create(timestamp, buildInformation))

  private val databaseHealthCheck: F[HealthStatus] =
    transaction(sql"SELECT 1".query[Int].unique)
      .map {
        case 1 => HealthStatus.Healthy
        case _ => HealthStatus.Unhealthy
      }

  private val internetConnectivityCheck: F[HealthStatus] =
    client
      .status(GET(HealthService.ConnectivityUrl))
      .map(status => if status == Status.Ok then HealthStatus.Healthy else HealthStatus.Unhealthy)

  private def runWithTimeout(run: F[HealthStatus]): F[HealthStatus] =
    Concurrent[F]
      .race(run, Clock[F].sleep(HealthService.Timeout))
      .map {
        case Left(healthStatus) => healthStatus
        case _ => HealthStatus.Unhealthy
      }

  override val healthCheck: F[HealthCheck] =
    for {
      databaseHealthFiber <- Concurrent[F].start(runWithTimeout(databaseHealthCheck))

      internetConnectivityHealthStatus <- runWithTimeout(internetConnectivityCheck)
      databaseHealthStatus <- databaseHealthFiber.joinWith(Applicative[F].pure(HealthStatus.Unhealthy))
    } yield HealthCheck(database = databaseHealthStatus, internetConnectivity = internetConnectivityHealthStatus)
}
