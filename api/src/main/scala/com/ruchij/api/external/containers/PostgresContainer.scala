package com.ruchij.api.external.containers

import cats.effect.{Resource, Sync}
import cats.implicits.*
import com.ruchij.migration.config.DatabaseConfiguration
import com.ruchij.api.external.ContainerizedExternalResourceProvider
import org.testcontainers.containers.PostgreSQLContainer

class PostgresContainer extends PostgreSQLContainer[PostgresContainer]("postgres:14")

object PostgresContainer {
  def create[F[_]: Sync]: Resource[F, DatabaseConfiguration] =
    Resource
      .eval {
        Sync[F].delay {
          new PostgresContainer()
            .withUsername("my-user")
            .withPassword("my-password")
            .withDatabaseName("uptime-monitor")
        }
      }
      .flatMap(postgresContainer => ContainerizedExternalResourceProvider.start(postgresContainer))
      .evalMap { postgresContainer =>
        Sync[F]
          .blocking(postgresContainer.getJdbcUrl())
          .map { jdbcUrl =>
            DatabaseConfiguration(
              jdbcUrl, 
              postgresContainer.getUsername(), 
              postgresContainer.getPassword()
            )
          }
      }
}
