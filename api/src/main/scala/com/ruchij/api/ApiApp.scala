package com.ruchij.api

import cats.arrow.FunctionK
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.kernel.Resource.apply
import cats.implicits.*
import cats.~>
import com.ruchij.api.config.ServiceConfiguration
import com.ruchij.api.dao.credentials.DoobieCredentialsDao
import com.ruchij.api.dao.doobie.DoobieTransactor
import com.ruchij.api.dao.user.DoobieUserDao
import com.ruchij.api.services.hash.BCryptPasswordHashingService
import com.ruchij.api.services.hash.PasswordHashingService
import com.ruchij.api.services.applicationhealth.ApplicationHealthService
import com.ruchij.api.services.applicationhealth.ApplicationHealthServiceImpl
import com.ruchij.api.services.user.UserService
import com.ruchij.api.services.user.UserServiceImpl
import com.ruchij.api.types.JodaClock
import com.ruchij.api.web.Routes
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.util.transactor
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource

object ApiApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      serviceConfiguration <- ServiceConfiguration.parse[IO](configObjectSource)

      exitCode <-
        httpApp[IO](serviceConfiguration)
          .flatMap { app =>
            EmberServerBuilder
              .default[IO]
              .withHttpApp(app)
              .withHost(serviceConfiguration.httpConfiguration.host)
              .withPort(serviceConfiguration.httpConfiguration.port)
              .build
          }
          .use(_ => IO.never)
          .as(ExitCode.Success)
    } yield exitCode

  def httpApp[F[_]: Async: JodaClock](serviceConfiguration: ServiceConfiguration): Resource[F, HttpApp[F]] =
    for {
      hikariTransactor <- DoobieTransactor.create(serviceConfiguration.databaseConfiguration)
      client <- EmberClientBuilder.default[F].build
    } yield httpApp(hikariTransactor, client, serviceConfiguration)

  def httpApp[F[_]: Async: JodaClock](
    hikariTransactor: HikariTransactor[F],
    client: Client[F],
    serviceConfiguration: ServiceConfiguration
  ): HttpApp[F] = {
    given FunctionK[ConnectionIO, F] = hikariTransactor.trans

    val applicationHealthService: ApplicationHealthService[F] =
      ApplicationHealthServiceImpl[F](client, serviceConfiguration.buildInformation)

    val passwordHashingService: PasswordHashingService[F] = BCryptPasswordHashingService[F]

    val userService: UserService[F] =
      UserServiceImpl[F, ConnectionIO](passwordHashingService, DoobieUserDao, DoobieCredentialsDao)

    Routes(userService, ???, applicationHealthService)
  }
}
