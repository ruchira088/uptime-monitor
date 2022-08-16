package com.ruchij.migration

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits.*
import com.ruchij.migration.config.MigrationConfiguration
import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.util.Locations
import com.ruchij.migration.models.MigrationResult
import pureconfig.ConfigSource
import com.ruchij.migration.config.DatabaseConfiguration
import com.typesafe.scalalogging.Logger.apply
import com.typesafe.scalalogging.Logger

object MigrationApp extends IOApp {
  private val logger = Logger[MigrationApp.type]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO.blocking(logger.info("Starting migration application..."))

      configObjectSource <- IO.blocking(ConfigSource.defaultApplication)
      migrationConfiguration <- IO.fromEither(MigrationConfiguration.load(configObjectSource))

      migrationResults <- migrate[IO](migrationConfiguration)

      _ <- IO.blocking(logger.info(s"Completed migration application: $migrationResults"))
    }
    yield ExitCode.Success

  def migrate[F[+_]: Sync](migrationConfiguration: MigrationConfiguration): F[List[MigrationResult] | MigrationResult] =
    migrationConfiguration.application
      .fold(migrateAll(migrationConfiguration.databaseConfiguration)) { application =>
        migrate(migrationConfiguration.databaseConfiguration, application)  
      }

  def migrateAll[F[_]: Sync](databaseConfiguration: DatabaseConfiguration): F[List[MigrationResult]] =
    Application.values.toList
      .traverse(application => migrate(databaseConfiguration, application))

  def migrate[F[_]: Sync](databaseConfiguration: DatabaseConfiguration, application: Application): F[MigrationResult] =
    for {
      flyway <-
        Sync[F].blocking {
          Flyway
            .configure()
            .dataSource(
              databaseConfiguration.url,
              databaseConfiguration.user,
              databaseConfiguration.password
            )
            .locations(Seq(application.flywayScripts): _*)
            .load()
        }

      flywayResult <- Sync[F].blocking(flyway.migrate())

    } yield MigrationResult(
      application, 
      flywayResult.initialSchemaVersion, 
      flywayResult.targetSchemaVersion, 
      flywayResult.migrationsExecuted
    )
}
