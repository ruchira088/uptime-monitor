package com.ruchij.api.external

import cats.effect.Sync
import cats.implicits.*
import com.ruchij.migration.config.DatabaseConfiguration
import cats.effect.kernel.Resource
import com.ruchij.api.config.RedisConfiguration
import com.ruchij.api.external.containers.PostgresContainer
import org.testcontainers.containers.{PostgisContainerProvider, PostgreSQLContainer}
import com.ruchij.api.external.containers.RedisContainer
import org.testcontainers.containers.GenericContainer

class ContainerizedExternalResourceProvider[F[_]: Sync] extends ExternalResourceProvider[F] {

  override val databaseConfiguration: Resource[F, DatabaseConfiguration] = 
    PostgresContainer.create[F]

  override val redisConfiguration: Resource[F, RedisConfiguration] =
    RedisContainer.create[F]
}

object ContainerizedExternalResourceProvider {
  def start[F[_]: Sync, A <: GenericContainer[A]](testContainer: A): Resource[F, A] =
    Resource.make[F, A](Sync[F].delay(testContainer.start()).as(testContainer)) { container =>
      Sync[F].delay {
        container.stop()
        container.close()
      }
    }
}
