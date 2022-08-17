package com.ruchij.api.dao.doobie

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.implicits.*
import com.ruchij.api.types.FunctionKTypes.{_, given}
import com.ruchij.migration.config.DatabaseConfiguration
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

import scala.concurrent.ExecutionContext

object DoobieTransactor {
    def create[F[_]: Async](databaseConfiguration: DatabaseConfiguration): Resource[F, HikariTransactor[F]] =
        for {
            connectEC <- ExecutionContexts.fixedThreadPool(8)
            transactor <- create[F](databaseConfiguration, connectEC)
        }
        yield transactor

    def create[F[_]: Async](databaseConfiguration: DatabaseConfiguration, connectEC: ExecutionContext): Resource[F, HikariTransactor[F]] =
        Resource.eval(DatabaseDriver.parse(databaseConfiguration.url).toType[F, Throwable])
            .flatMap { databaseDriver =>
                HikariTransactor.newHikariTransactor[F](
                    databaseDriver.driver,
                    databaseConfiguration.url,
                    databaseConfiguration.user,
                    databaseConfiguration.password,
                    connectEC
                )
            }

}
