package com.ruchij.api.test.mixins

import org.scalatest.exceptions.TestFailedException
import org.scalactic.source.Position
import org.scalatest.exceptions.StackDepthException
import org.scalatest.Resources

trait EitherValues {
  extension [L, R] (either: Either[L, R])(using position: Position) {
    def value: R = 
      either.fold[R](
        left => 
          throw TestFailedException(
            (_: StackDepthException) => Some(s"Expected right value, but left=$left"), 
            None, 
            position
          ), 
        identity
      )
  }
}
