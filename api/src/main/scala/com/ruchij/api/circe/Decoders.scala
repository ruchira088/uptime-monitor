package com.ruchij.api.circe

import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import io.circe.Decoder
import org.joda.time.DateTime

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
