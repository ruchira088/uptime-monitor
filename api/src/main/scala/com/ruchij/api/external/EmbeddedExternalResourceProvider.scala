package com.ruchij.api.external

import cats.data.OptionT
import cats.effect.Resource
import cats.effect.Sync
import cats.implicits.*
import com.comcast.ip4s.Port
import com.comcast.ip4s.ipv4
import com.ruchij.api.config.RedisConfiguration
import com.ruchij.migration.config.DatabaseConfiguration
import redis.embedded.RedisServer
import com.ruchij.api.types.RandomGenerator
import java.util.UUID

class EmbeddedExternalResourceProvider[F[_]: Sync] extends ExternalResourceProvider[F] {
  override val databaseConfiguration: Resource[F, DatabaseConfiguration] =
    Resource.eval(RandomGenerator[F, UUID].generate)
      .map { uuid => 
        DatabaseConfiguration(
          s"jdbc:h2:mem:uptime-monitor-${uuid.toString().take(8)};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
          "",
          ""
        )
      }

  override val redisConfiguration: Resource[F, RedisConfiguration] =
    Resource.eval(ExternalResourceProvider.freePort[F])
      .flatTap { port =>
        Resource.make {
          Sync[F].delay(RedisServer.builder().port(port).build())
            .flatTap { redisServer => Sync[F].blocking(redisServer.start())}
        } (redisServer => Sync[F].blocking(redisServer.stop()))
      }
      .evalMap { port => 
        OptionT.fromOption[F](Port.fromInt(port))
          .getOrRaise {
            IllegalArgumentException(s"$port is not in the valid port range") 
          }
      }
      .map { port => RedisConfiguration(ipv4"127.0.0.1", port, None) }
}
