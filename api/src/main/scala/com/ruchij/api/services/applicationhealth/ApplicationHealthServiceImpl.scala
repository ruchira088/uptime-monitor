package com.ruchij.api.services.applicationhealth

import cats.effect.{Async, Clock, Concurrent, Sync}
import cats.implicits.*
import cats.~>
import com.ruchij.api.services.applicationhealth.models.{HealthStatus, ServiceHealthStatus, ServiceInformation}
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
import com.ruchij.api.kvstore.KeyValueStore
import com.ruchij.api.types.IdGenerator
import com.ruchij.api.dao.models.IDs.ID
import cats.effect.kernel.Fiber

class ApplicationHealthServiceImpl[F[_]: JodaClock: Async: IdGenerator](
  client: Client[F],
  keyValueStore: KeyValueStore[F]
)(using transaction: ConnectionIO ~> F)
    extends ApplicationHealthService[F] {
  private val http4sClientDsl = new Http4sClientDsl[F] {}

  import http4sClientDsl._

  override val serviceInformation: F[ServiceInformation] =
    JodaClock[F].timestamp
      .flatMap(timestamp => ServiceInformation.create(timestamp))

  private val databaseHealthCheck: F[HealthStatus] =
    transaction(sql"SELECT 1".query[Int].unique)
      .map {
        case 1 => HealthStatus.Healthy
        case _ => HealthStatus.Unhealthy
      }

  private val keyValueStoreCheck: F[HealthStatus] =
    for {
      key <- IdGenerator[F].generate[String].map(_.toString)
      value <- IdGenerator[F].generate[String].map(_.toString)

      _ <- keyValueStore.put(key, value, None)
      persistedValue <- keyValueStore.get[String, String](key)

      _ <- keyValueStore.delete(key)

      healthStatus = if persistedValue == Some(value) then HealthStatus.Healthy else HealthStatus.Unhealthy
    } yield healthStatus

  private val internetConnectivityCheck: F[HealthStatus] =
    client
      .status(GET(ApplicationHealthService.ConnectivityUrl))
      .map(status => if status == Status.Ok then HealthStatus.Healthy else HealthStatus.Unhealthy)

  private def runWithTimeout(run: F[HealthStatus]): F[HealthStatus] =
    Concurrent[F]
      .race(run, Clock[F].sleep(ApplicationHealthService.Timeout))
      .map {
        case Left(healthStatus) => healthStatus
        case _ => HealthStatus.Unhealthy
      }
      .recover { _ => HealthStatus.Unhealthy }

  private def waitForHealthStatus(fiber: Fiber[F, Throwable, HealthStatus]): F[HealthStatus] =
    fiber.joinWith(Applicative[F].pure(HealthStatus.Unhealthy))

  override val healthCheck: F[ServiceHealthStatus] =
    for {
      databaseHealthFiber <- Concurrent[F].start(runWithTimeout(databaseHealthCheck))
      keyValueStoreHealthFiber <- Concurrent[F].start(runWithTimeout(keyValueStoreCheck))
      internetConnectivityHealthStatus <- runWithTimeout(internetConnectivityCheck)

      databaseHealthStatus <- waitForHealthStatus(databaseHealthFiber)
      keyValueStoreHealthStatus <- waitForHealthStatus(keyValueStoreHealthFiber)

      serviceHealthStatus =
        ServiceHealthStatus(
          database = databaseHealthStatus,
          keyValueStore = keyValueStoreHealthStatus,
          internetConnectivity = internetConnectivityHealthStatus
        )

    } yield serviceHealthStatus
}
