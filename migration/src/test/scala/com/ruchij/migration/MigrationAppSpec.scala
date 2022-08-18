package com.ruchij.migration

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.ruchij.migration.config.DatabaseConfiguration

import scala.language.postfixOps
import scala.concurrent.duration.DurationInt

class MigrationAppSpec extends AnyFlatSpec with Matchers {
  "migrateAll(DatabaseConfiguration)" should "perform migrations without any errors" in {
    val result = MigrationApp
      .migrateAll[IO](MigrationAppSpec.DatabaseConfig)
      .unsafeRunTimed(10 seconds)

    result mustBe defined
  }
}

object MigrationAppSpec {
  val DatabaseConfig: DatabaseConfiguration =
    DatabaseConfiguration(
      "jdbc:h2:mem:uptime-monitor;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
      "",
      ""
    )
}
