package com.ruchij.api.test.mixins.io

import cats.effect.IO
import com.ruchij.api.test.mixins.MockedRoutes
import org.scalatest.Suite
import cats.effect.kernel.Async

trait MockedRoutesIO extends MockedRoutes[IO] { self: Suite =>
  override val async: Async[IO] = IO.asyncForIO
}
