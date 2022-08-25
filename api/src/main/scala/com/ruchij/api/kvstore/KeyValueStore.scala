package com.ruchij.api.kvstore

import io.circe.{Decoder, Encoder}

import scala.concurrent.duration.FiniteDuration

trait KeyValueStore[F[_]] {
  def put[K: Encoder, V: Encoder](key: K, value: V, ttl: Option[FiniteDuration]): F[Unit]

  def get[K: Encoder, V: Decoder](key: K): F[Option[V]]

  def delete[K: Encoder](key: K): F[Unit]
}