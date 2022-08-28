package com.ruchij.api.circe

import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret
import com.ruchij.api.types.IdGenerator.IdPrefix
import io.circe.Decoder
import org.http4s.{Header, Method, Uri}
import org.joda.time.DateTime

import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror
import scala.reflect.ClassTag
import scala.util.Try

object Decoders {
  given Decoder[DateTime] =
    Decoder.decodeString.emapTry(stringValue => Try(DateTime.parse(stringValue)))
    
  given Decoder[Email] =
    Decoder.decodeString.emap(stringValue => Email(stringValue).left.map(_.message))
    
  given Decoder[Password] =
    Decoder.decodeString.map(stringValue => Password(stringValue))

  given Decoder[AuthenticationSecret] =
    Decoder.decodeString.map(stringValue => AuthenticationSecret(stringValue))

  given [A](using IdPrefix[A], ClassTag[A]): Decoder[ID[A]] =
    Decoder.decodeString.emap { stringValue => ID.parse[A](stringValue).left.map(_.message) }

  given Decoder[Method] = Decoder.decodeString.emap(stringValue => Method.fromString(stringValue).left.map(_.message))

  given Decoder[Uri] = Decoder.decodeString.emap(stringValue => Uri.fromString(stringValue).left.map(_.message))

  inline given [A <: Enum[A]](using mirror: Mirror.SumOf[A], classTag: ClassTag[A]): Decoder[A] = {
    val enumValues = values[mirror.MirroredElemTypes, A]

    Decoder.decodeString.emap { enumString => 
      enumValues.find(_.name().equalsIgnoreCase(enumString.trim()))
        .toRight {
          s"""Unable to decode "$enumString" as  ${classTag.runtimeClass.getSimpleName()}. """ +
          s"Possible values: ${enumValues.map(_.name()).mkString("[", ", ", "]")}"
        }
    }
  }

  private inline def values[T <: Tuple, A <: Enum[A]]: List[A] =
    inline erasedValue[T] match
      case EmptyTuple => Nil
      case _: (head *: tail) => 
        inline summonInline[Mirror.Of[head]] match
          case value: A =>
            value :: values[tail, A]
}
