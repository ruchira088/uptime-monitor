package com.ruchij.api.circe

import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret
import io.circe.Decoder
import org.joda.time.DateTime
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.types.IdGenerator.IdPrefix

import scala.util.Try
import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror
import scala.reflect.ClassTag

object Decoders {
  given Decoder[DateTime] =
    Decoder.decodeString.emapTry(dateTimeString => Try(DateTime.parse(dateTimeString)))
    
  given Decoder[Email] =
    Decoder.decodeString.emap(emailString => Email(emailString).left.map(_.message))
    
  given Decoder[Password] =
    Decoder.decodeString.map(password => Password(password))

  given Decoder[AuthenticationSecret] =
    Decoder.decodeString.map(secret => AuthenticationSecret(secret))

  given [A](using IdPrefix[A], ClassTag[A]): Decoder[ID[A]] =
    Decoder.decodeString.emap { id => ID.parse[A](id).left.map(_.message) }

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
