package com.ruchij.api.dao.doobie

import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.types.IdGenerator.IdPrefix
import doobie.enumerated.JdbcType.Date
import doobie.implicits.javasql.*
import doobie.util.Get
import doobie.util.Put
import org.http4s.{Header, Method, Uri}
import org.joda.time.DateTime
import org.tpolecat.typename.TypeName
import org.http4s.headers.`Content-Type`

import java.sql.Timestamp
import scala.reflect.ClassTag
import org.http4s.Header.apply

import java.util.Base64
import scala.util.Try

object DoobieCodecs {
    given Put[Email] = Put[String].tcontramap[Email](_.toString)

    given Get[Email] = Get[String].temap(stringValue => Email(stringValue).left.map(_.message))

    given Put[DateTime] = Put[Timestamp].tcontramap[DateTime](dateTime => Timestamp(dateTime.getMillis))

    given Get[DateTime] = Get[Timestamp].tmap(stringValue => DateTime(stringValue.getTime))

    given Put[HashedPassword] = Put[String].tcontramap[HashedPassword](_.toString)

    given Get[HashedPassword] = Get[String].map(stringValue => HashedPassword(stringValue))

    given [A]: Put[ID[A]] = Put[String].tcontramap[ID[A]](_.toString)

    given [A: IdPrefix: ClassTag]: Get[ID[A]] = Get[String].temap(stringValue => ID.parse(stringValue).left.map(_.message))

    given Put[Method] = Put[String].tcontramap(_.name)

    given Get[Method] = Get[String].temap(stringValue => Method.fromString(stringValue).left.map(_.message))

    given Put[Uri] = Put[String].tcontramap(_.toString)

    given Get[Uri] = Get[String].temap(stringValue => Uri.fromString(stringValue).left.map(_.message))

    given Put[`Content-Type`] = Put[String].tcontramap(contentType => Header[`Content-Type`].value(contentType))

    given Get[`Content-Type`] =
      Get[String].temap(stringValue => Header[`Content-Type`].parse(stringValue).left.map(_.message))

    given Put[Seq[Byte]] =
      Put[String].tcontramap { bytes => Base64.getEncoder.encodeToString(bytes.toArray) }

    given Get[Seq[Byte]] =
      Get[String].temap { stringValue =>
        Try(Base64.getDecoder.decode(stringValue)).toEither
          .map(_.toSeq)
          .left.map(_.getMessage)
      }
}
