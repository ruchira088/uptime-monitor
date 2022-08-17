package com.ruchij.api.config

import cats.effect.IO
import com.comcast.ip4s.{ipv4, port}
import com.ruchij.api.test.utils.IOUtils.{IOErrorOps, runIO}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import pureconfig.ConfigSource
import com.ruchij.migration.config.DatabaseConfiguration.apply
import com.ruchij.migration.config.DatabaseConfiguration

class ServiceConfigurationSpec extends AnyFlatSpec with Matchers {

  "ServiceConfiguration" should "parse the ConfigObjectSource" in runIO {
    val configObjectSource =
      ConfigSource.string {
        s"""
          database-configuration {
            url = "jdbc:h2:mem:uptime-monitor;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
            url = $${?DATABASE_URL}

            user = ""
            user = $${?DATABASE_USER}

            password = ""
            password = $${?DATABASE_PASSWORD}
          }

          http-configuration {
            host = "127.0.0.1"
            host = $${?HTTP_HOST}

            port = 80
            port = $${?HTTP_PORT}
          }

          build-information {
            git-branch = "my-branch"

            git-commit = $${?GIT_COMMIT}

            build-timestamp = "2021-07-31T10:10:00.000Z"
          }
        """
      }

    ServiceConfiguration.parse[IO](configObjectSource).flatMap {
      serviceConfiguration =>
        IO.delay {
          serviceConfiguration.httpConfiguration mustBe HttpConfiguration(ipv4"127.0.0.1", port"80")
          serviceConfiguration.buildInformation mustBe
            BuildInformation(Some("my-branch"), None, Some(DateTime(2021, 7, 31, 10, 10, 0, 0, DateTimeZone.UTC)))
          serviceConfiguration.databaseConfiguration mustBe
            DatabaseConfiguration("jdbc:h2:mem:uptime-monitor;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", "", "")
        }
    }
  }

  it should "return an error if ConfigObjectSource is not parsable" in runIO {
    val configObjectSource =
      ConfigSource.string {
        s"""
          database-configuration {
            url = "jdbc:h2:mem:uptime-monitor;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
            user = ""
            password = ""
          }

          http-configuration {
            host = "0.0.0.0"

            port = 8080
          }

          build-information {
            git-branch = "my-branch"

            build-timestamp = "invalid-date"
          }
        """
      }

    ServiceConfiguration.parse[IO](configObjectSource).error
      .flatMap { throwable =>
        IO.delay {
          throwable.getMessage must include("Cannot convert 'invalid-date' to DateTime: Invalid format: \"invalid-date\"")
        }
      }
  }

}
