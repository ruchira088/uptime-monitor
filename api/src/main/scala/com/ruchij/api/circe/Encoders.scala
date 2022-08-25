package com.ruchij.api.circe

import com.ruchij.api.dao.user.models.Emails.Email
import io.circe.Encoder
import org.joda.time.DateTime
import com.ruchij.api.dao.models.IDs.ID

import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret

object Encoders {
  given Encoder[DateTime] = Encoder.encodeString.contramap[DateTime](_.toString)

  given Encoder[Email] = Encoder.encodeString.contramap(_.toString)

  given Encoder[AuthenticationSecret] = Encoder.encodeString.contramap(_.toString)

  given [A]: Encoder[ID[A]] = Encoder.encodeString.contramap(_.toString)

  given [A <: Enum[A]]: Encoder[A] = Encoder.encodeString.contramap[A](_.name())
}
