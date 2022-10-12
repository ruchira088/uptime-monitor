package com.ruchij.api.external

import cats.effect.{MonadCancelThrow, Resource, Sync}
import cats.implicits.*
import com.ruchij.api.config.RedisConfiguration
import com.ruchij.api.types.RandomGenerator
import com.ruchij.migration.config.DatabaseConfiguration

import scala.util.Random
import cats.MonadThrow
import java.net.ServerSocket

trait ExternalResourceProvider[F[_]] {
  val databaseConfiguration: Resource[F, DatabaseConfiguration]

  val redisConfiguration: Resource[F, RedisConfiguration]
}

object ExternalResourceProvider {
  def freePort[F[_]: Sync]: F[Int] =
    RandomGenerator.range(3000, 10_000).generate[Int]
      .flatMap { port => 
        MonadThrow[F].handleErrorWith {
          MonadCancelThrow[F].uncancelable { _ =>
            Sync[F]
              .blocking(new ServerSocket(port))
              .flatMap { serverSocket =>
                Sync[F].blocking(serverSocket.close())
              }
              .as(port)
          }
        } { _ => freePort[F] }
      }
}
