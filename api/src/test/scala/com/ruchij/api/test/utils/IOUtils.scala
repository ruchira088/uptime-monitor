package com.ruchij.api.test.utils

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.checkerframework.checker.units.qual.h

object IOUtils {

  def runIO[A](block: => IO[A]): A = block.unsafeRunSync()

  extension [A] (io: IO[A]) {
    def error: IO[Throwable] =
      io.attempt.flatMap {
        case Left(throwable) => IO.pure(throwable)
        case Right(success) => IO.raiseError(IllegalStateException(s"Expected an exception, but returned $success"))
      }
  }
}
