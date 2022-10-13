package com.ruchij.stack

import cats.Monad
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import com.comcast.ip4s.ipv4
import com.comcast.ip4s.port
import com.ruchij.api.ApiApp
import com.ruchij.api.config.AuthenticationConfiguration
import com.ruchij.api.config.HttpConfiguration
import com.ruchij.api.config.RedisConfiguration
import com.ruchij.api.config.ServiceConfiguration
import com.ruchij.api.external.ContainerizedExternalResourceProvider
import com.ruchij.api.external.ExternalResourceProvider
import com.ruchij.api.types.JodaClock
import com.ruchij.migration.Application
import com.ruchij.migration.MigrationApp
import com.ruchij.migration.config.DatabaseConfiguration
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import pureconfig.ConfigObjectSource
import pureconfig.ConfigSource

import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

object ApplicationStack extends IOApp {

  val AuthenticationConfig = AuthenticationConfiguration(7 days)

  val HttpConfig: HttpConfiguration = HttpConfiguration(ipv4"0.0.0.0", port"8080")

  override def run(args: List[String]): IO[ExitCode] =
    serviceConfig(ContainerizedExternalResourceProvider[IO])
      .evalTap(serviceConfiguration => MigrationApp.migrateAll(serviceConfiguration.databaseConfiguration))
      .flatMap(serviceConfiguration => api[IO](serviceConfiguration))
      .use(_ => IO.never)
      .as(ExitCode.Success)

  private def api[F[_]: Async: JodaClock](serviceConfiguration: ServiceConfiguration): Resource[F, Server] =
    ApiApp
      .httpApp[F](serviceConfiguration)
      .flatMap { httpApp =>
        EmberServerBuilder
          .default[F]
          .withHttpApp(httpApp)
          .withHost(serviceConfiguration.httpConfiguration.host)
          .withPort(serviceConfiguration.httpConfiguration.port)
          .build
      }

  private def serviceConfig[F[_]: Monad](
    externalResourceProvider: ExternalResourceProvider[F]
  ): Resource[F, ServiceConfiguration] =
    for {
      databaseConfiguration <- externalResourceProvider.databaseConfiguration
      redisConfiguration <- externalResourceProvider.redisConfiguration

      serviceConfiguration =
        ServiceConfiguration(databaseConfiguration, redisConfiguration, AuthenticationConfig, HttpConfig)
    } yield serviceConfiguration

}
