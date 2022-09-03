package com.ruchij.api.kvstore

import cats.data.OptionT
import cats.{Monad, MonadThrow}
import cats.implicits.*
import com.ruchij.api.config.RedisConfiguration
import io.circe.{Decoder, Encoder, Json}
import io.circe.parser as JsonParser
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.effects.SetArgs
import dev.profunktor.redis4cats.effects.SetArg.Ttl.Px
import com.ruchij.api.types.FunctionKTypes.{*, given}
import dev.profunktor.redis4cats.effect.MkRedis.forAsync
import dev.profunktor.redis4cats.effect.Log.NoOp.instance

import java.nio.ByteBuffer
import scala.concurrent.duration.FiniteDuration
import cats.effect.kernel.Resource
import cats.effect.kernel.Async

class RedisKeyValueStore[F[_]: MonadThrow](redisCommands: RedisCommands[F, String, String]) extends KeyValueStore[F] {

  override def put[K: Encoder, V: Encoder](key: K, value: V, ttl: Option[FiniteDuration]): F[Unit] = {
    val encodedKey = Encoder[K].apply(key).noSpacesSortKeys
    val encodedValue = Encoder[V].apply(value).noSpacesSortKeys

    redisCommands.set(encodedKey, encodedValue, SetArgs(None, ttl.map(Px.apply)))
      .as((): Unit)
  }

  override def get[K: Encoder, V: Decoder](key: K): F[Option[V]] = {
    val encodedKey = Encoder[K].apply(key).noSpacesSortKeys

    OptionT(redisCommands.get(encodedKey))
      .semiflatMap {
        stringValue =>
          JsonParser.parse(stringValue)
            .flatMap(json => Decoder[V].decodeJson(json))
            .toType[F, Throwable]
      }
      .value
  }

  override def delete[K: Encoder](key: K): F[Unit] = {
    val encodedKey = Encoder[K].apply(key).noSpacesSortKeys

    redisCommands.del(encodedKey).as((): Unit)
  }

}

object RedisKeyValueStore {
  def create[F[_]: Async](redisConfiguration: RedisConfiguration): Resource[F, RedisCommands[F, String, String]] =
    Redis.apply[F].utf8(redisConfiguration.url)
}