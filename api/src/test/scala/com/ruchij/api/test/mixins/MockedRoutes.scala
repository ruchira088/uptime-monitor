package com.ruchij.api.test.mixins

import cats.effect.kernel.Sync
import com.ruchij.api.services.applicationhealth.ApplicationHealthService
import com.ruchij.api.web.Routes
import org.http4s.HttpApp
import org.scalatest.{OneInstancePerTest, Suite}
import org.scalatestplus.mockito.MockitoSugar
import cats.effect.kernel.Async
import com.ruchij.api.services.authentication.AuthenticationService
import com.ruchij.api.services.user.UserService

trait MockedRoutes[F[_]] extends MockitoSugar with OneInstancePerTest {  self: Suite =>

  val userService: UserService[F] = mock[UserService[F]]
  val authenticationService: AuthenticationService[F] = mock[AuthenticationService[F]]
  val applicationHealthService: ApplicationHealthService[F] = mock[ApplicationHealthService[F]]

  val async: Async[F]

  def createRoutes: HttpApp[F] =
    Routes[F](userService, authenticationService, applicationHealthService)(async)

}
