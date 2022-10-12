package com.ruchij.api.external.containers

import cats.data.OptionT
import cats.effect.{MonadCancel, Resource, Sync}
import cats.implicits.*
import com.comcast.ip4s.Host
import com.ruchij.api.config.RedisConfiguration
import com.ruchij.api.external.containers.RedisContainer
import com.ruchij.api.external.ContainerizedExternalResourceProvider
import org.testcontainers.containers.GenericContainer
import com.comcast.ip4s.Port
import com.ruchij.api.dao.user.models.Passwords

class RedisContainer extends GenericContainer[RedisContainer]("bitnami/redis:7.0")

object RedisContainer {
  private val RedisPort = 6379
  private val Password = "my-password"

  def create[F[_]: Sync]: Resource[F, RedisConfiguration] =
    Resource.eval(Sync[F].delay(new RedisContainer().withExposedPorts(RedisPort).withEnv("REDIS_PASSWORD", Password)))
      .flatMap(redisContainer => ContainerizedExternalResourceProvider.start(redisContainer))
      .evalMap { redisContainer =>
         for {
          hostString <- Sync[F].blocking(redisContainer.getHost)
          host <- 
            OptionT.fromOption[F](Host.fromString(hostString))
              .getOrRaise(IllegalArgumentException(s"$hostString is not a valid host"))

          portNumber <- Sync[F].blocking(redisContainer.getMappedPort(RedisPort))
          port <- 
            OptionT.fromOption[F](Port.fromInt(portNumber))
              .getOrRaise(IllegalAccessException(s"$portNumber is not a valid port"))
        }
        yield RedisConfiguration(host, port, Some(Passwords.Password(Password)))
      }
}
