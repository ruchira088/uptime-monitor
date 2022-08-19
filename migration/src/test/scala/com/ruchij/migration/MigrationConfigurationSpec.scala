package com.ruchij.migration

import org.scalatest.flatspec.AnyFlatSpec
import pureconfig.ConfigSource
import com.ruchij.migration.config.MigrationConfiguration
import org.scalatest.matchers.must.Matchers
import com.ruchij.migration.config.DatabaseConfiguration

class MigrationConfigurationSpec extends AnyFlatSpec with Matchers {

  "load(ConfigObjectSource)" should "read migration configuration" in {
    val configObjectSource =
      ConfigSource.string {
        s"""
            database-configuration {
                url = "${MigrationAppSpec.DatabaseConfig.url}"
                user = "admin"
                password = "my-password"
            }

            application = "api"
        """
      }

    MigrationConfiguration.load(configObjectSource) mustBe
      Right {
        MigrationConfiguration(
          DatabaseConfiguration(MigrationAppSpec.DatabaseConfig.url, "admin", "my-password"),
          Some(Application.Api)
        )
      }
  }

}
