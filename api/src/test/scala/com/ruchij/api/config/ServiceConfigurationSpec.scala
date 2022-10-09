package com.ruchij.api.config

import cats.effect.IO
import com.comcast.ip4s.Literals.host
import com.comcast.ip4s.{host, ipv4, port}
import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.test.utils.IOUtils.{error, runIO}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import pureconfig.ConfigSource
import com.ruchij.migration.config.DatabaseConfiguration.apply
import com.ruchij.migration.config.DatabaseConfiguration

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

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

          redis-configuration {
            host = "localhost"
            host = $${?REDIS_HOST}

            port = 6379
            port = $${?REDIS_PORT}

            password = "my-password"
            password = $${?REDIS_PASSWORD}
          }

          authentication-configuration {
            session-duration = "7d"
          }

          http-configuration {
            host = "127.0.0.1"
            host = $${?HTTP_HOST}

            port = 80
            port = $${?HTTP_PORT}
          }
        """
      }

    ServiceConfiguration.parse[IO](configObjectSource).flatMap {
      serviceConfiguration =>
        IO.delay {
          serviceConfiguration.httpConfiguration mustBe HttpConfiguration(ipv4"127.0.0.1", port"80")

          serviceConfiguration.redisConfiguration mustBe RedisConfiguration(host"localhost", port"6379", Some(Password("my-password")))
          serviceConfiguration.redisConfiguration.url mustBe "redis://my-password@localhost:6379"

          serviceConfiguration.authenticationConfiguration mustBe AuthenticationConfiguration(7 days)

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

          redis-configuration {
            host = "localhost"
            port = 6379
            password = "my-password"
          }

          authentication-configuration {
            session-duration = "7d"
          }

          http-configuration {
            host = "0.0.0.0"
            port = my-invalid-port
          }
        """
      }

    ServiceConfiguration.parse[IO](configObjectSource).error
      .flatMap { throwable =>
        IO.delay {
          throwable.getMessage must include("Cannot convert 'my-invalid-port' to com.comcast.ip4s.Port")
        }
      }
  }

}
