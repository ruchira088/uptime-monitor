package com.ruchij.api.circe

import cats.Show
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret
import io.circe.Encoder
import org.http4s.headers.`Content-Type`
import org.http4s.{Header, Method, Uri}
import org.joda.time.DateTime

import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror

object Encoders {
  given Encoder[DateTime] = Encoder.encodeString.contramap[DateTime](_.toString)

  given Encoder[Email] = Encoder.encodeString.contramap(_.toString)

  given Encoder[AuthenticationSecret] = Encoder.encodeString.contramap(_.toString)

  given Encoder[Method] = Encoder.encodeString.contramap(_.name)

  given Encoder[Uri] = Encoder.encodeString.contramap(_.renderString)

  given Encoder[`Content-Type`] = Encoder.encodeString.contramap(contentType => Header[`Content-Type`].value(contentType))

  given [A]: Encoder[ID[A]] = Encoder.encodeString.contramap(_.toString)

  given [A <: Enum[A]]: Encoder[A] = Encoder.encodeString.contramap[A](_.name())
}
