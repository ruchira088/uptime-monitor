package com.ruchij.api

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.*
import com.ruchij.api.config.ServiceConfiguration
import com.ruchij.api.services.health.HealthServiceImpl
import com.ruchij.api.web.Routes
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource
import com.ruchij.api.dao.doobie.DoobieTransactor
import cats.effect.kernel.Async
import doobie.util.transactor
import doobie.ConnectionIO
import com.ruchij.api.services.health.HealthService
import com.ruchij.api.services.user.UserService
import com.ruchij.api.services.hash.PasswordHashingService
import com.ruchij.api.services.hash.BCryptPasswordHashingService
import com.ruchij.api.services.user.UserServiceImpl
import com.ruchij.api.dao.user.DoobieUserDao
import com.ruchij.api.dao.credentials.DoobieCredentialsDao
import com.ruchij.api.types.JodaClock
import cats.effect.kernel.Resource.apply
import cats.effect.kernel.Resource
import org.http4s.HttpApp

object ApiApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      serviceConfiguration <- ServiceConfiguration.parse[IO](configObjectSource)

      exitCode <- EmberServerBuilder
        .default[IO]
        .withHost(serviceConfiguration.httpConfiguration.host)
        .withPort(serviceConfiguration.httpConfiguration.port)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield exitCode

  def application[F[_]: Async: JodaClock](serviceConfiguration: ServiceConfiguration): Resource[F, HttpApp[F]] =
    DoobieTransactor.create(serviceConfiguration.databaseConfiguration)
      .map(_.trans)
      .map {
        implicit transactor => 
          val healthService: HealthService[F] = 
            HealthServiceImpl[F](serviceConfiguration.buildInformation)

          val passwordHashingService: PasswordHashingService[F] = BCryptPasswordHashingService[F]

          val userService: UserService[F] =
            UserServiceImpl[F, ConnectionIO](passwordHashingService, DoobieUserDao, DoobieCredentialsDao)

          Routes(userService, healthService)
      }
}
