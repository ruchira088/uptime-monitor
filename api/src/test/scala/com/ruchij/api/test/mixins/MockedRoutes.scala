package com.ruchij.api.test.mixins

import cats.effect.kernel.Sync
import com.ruchij.api.services.health.HealthService
import com.ruchij.api.web.Routes
import org.http4s.HttpApp
import org.scalatest.{OneInstancePerTest, Suite}
import org.scalatestplus.mockito.MockitoSugar
import cats.effect.kernel.Async
import com.ruchij.api.services.user.UserService

trait MockedRoutes[F[_]] extends MockitoSugar with OneInstancePerTest {  self: Suite =>

  val userService: UserService[F] = mock[UserService[F]]
  val healthService: HealthService[F] = mock[HealthService[F]]

  val async: Async[F]

  def createRoutes: HttpApp[F] =
    Routes[F](userService, healthService)(async)

}
