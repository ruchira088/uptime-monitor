package com.ruchij.api.types

import cats.implicits.*
import cats.Monad
import cats.Applicative.apply
import cats.Applicative
import cats.effect.kernel.Sync
import java.util.UUID

trait RandomGenerator[F[_], +A] {
  def generate[B >: A]: F[B]
}

object RandomGenerator {
  def apply[F[_], A](using randomGenerator: RandomGenerator[F, A]): RandomGenerator[F, A] =
    randomGenerator

  given [F[_]: Monad]: Monad[[X] =>> RandomGenerator[F, X]] with {
    def pure[A](x: A): RandomGenerator[F, A] = 
      new RandomGenerator[F, A] {
        override def generate[B >: A]: F[B] = Applicative[F].pure(x)
      }

    def flatMap[A, B](randomGenerator: RandomGenerator[F, A])(f: A => RandomGenerator[F, B]): RandomGenerator[F, B] =
      new RandomGenerator[F, B] {
        override def generate[C >: B]: F[C] = 
          randomGenerator.generate.flatMap { valueA => f(valueA).generate }
      }

    def tailRecM[A, B](a: A)(f: A => RandomGenerator[F, Either[A, B]]): RandomGenerator[F, B] = 
      new RandomGenerator[F, B] {
        override def generate[C >: B]: F[C] = 
          f(a).generate
            .flatMap { 
              case Left(value) => tailRecM(value)(f).generate
              case Right(value) =>  Applicative[F].pure(value)
            }
      }
  }

  given [F[_]: Sync]: RandomGenerator[F, UUID] with {
    override def generate[B >: UUID]: F[B] = Sync[F].delay(UUID.randomUUID())
  }
   
}