package com.ruchij.api.test.mixins

import cats.effect.kernel.Sync
import com.ruchij.api.services.health.HealthService
import com.ruchij.api.web.Routes
import org.http4s.HttpApp
import org.scalatest.{OneInstancePerTest, Suite}
import org.scalatestplus.mockito.MockitoSugar

trait MockedRoutes[F[_]] extends MockitoSugar with OneInstancePerTest {  self: Suite =>

  val healthService: HealthService[F] = mock[HealthService[F]]

  val sync: Sync[F]

  def createRoutes(): HttpApp[F] =
    Routes[F](healthService)(sync)

}
