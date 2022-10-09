package com.ruchij.stack

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.{Async, Resource}
import cats.effect.kernel.Resource.apply
import com.comcast.ip4s.{ipv4, port}
import com.ruchij.api.ApiApp
import com.ruchij.api.config.{HttpConfiguration, RedisConfiguration, ServiceConfiguration}
import com.ruchij.api.types.JodaClock
import com.ruchij.migration.{Application, MigrationApp}
import com.ruchij.migration.config.DatabaseConfiguration
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import pureconfig.{ConfigObjectSource, ConfigSource}
import com.ruchij.api.config.AuthenticationConfiguration

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

object ApplicationStack extends IOApp {

  val DatabaseConfig: DatabaseConfiguration =
    DatabaseConfiguration(
      "jdbc:h2:mem:uptime-monitor;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
      "",
      ""
    )
    
  val RedisConfig: RedisConfiguration = ???

  val AuthenticationConfig = AuthenticationConfiguration(7 days)

  val HttpConfig: HttpConfiguration = HttpConfiguration(ipv4"0.0.0.0", port"8080")

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- MigrationApp.migrate[IO](DatabaseConfig, Application.Api)
      _ <- api[IO].use(_ => IO.never)
    } yield ExitCode.Success

  private def api[F[_]: Async: JodaClock]: Resource[F, Server] =
    ApiApp
      .httpApp[F](ServiceConfiguration(DatabaseConfig, RedisConfig, AuthenticationConfig, HttpConfig))
      .flatMap { httpApp =>
        EmberServerBuilder
          .default[F]
          .withHttpApp(httpApp)
          .withHost(HttpConfig.host)
          .withPort(HttpConfig.port)
          .build
      }

}
