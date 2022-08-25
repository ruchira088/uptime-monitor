package com.ruchij.api.kvstore

import scala.concurrent.duration.FiniteDuration

class KeyspacedKeyValueStore[F[_], K, V](keyValueStore: KeyValueStore[F])(using keyspace: Keyspace[K, V]) {

  def put(key: K, value: V, ttl: Option[FiniteDuration]): F[Unit] =
    keyValueStore.put(keyspace.key(key), value, ttl)(using keyspace.keyEncoder, keyspace.valueEncoder)

  def get(key: K): F[Option[V]] =
    keyValueStore.get(keyspace.key(key))(using keyspace.keyEncoder, keyspace.valueDecoder)

  def delete(key: K): F[Unit] = keyValueStore.delete(keyspace.key(key))(using keyspace.keyEncoder)

}
