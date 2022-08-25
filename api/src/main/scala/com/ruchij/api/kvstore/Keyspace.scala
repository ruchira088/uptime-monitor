package com.ruchij.api.kvstore

import cats.Semigroup
import io.circe.{Codec, Decoder, Encoder}

class Keyspace[K, V](scope: K)(using
  val keyEncoder: Encoder[K],
  keySemigroup: Semigroup[K],
  val valueEncoder: Encoder[V],
  val valueDecoder: Decoder[V]
) {
  def key(key: K): K = keySemigroup.combine(scope, key)
}
