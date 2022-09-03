package com.ruchij.api.config

import com.comcast.ip4s.{Host, Port}
import com.ruchij.api.dao.user.models.Passwords.Password
import org.joda.time.DateTime
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

import scala.util.Try

object ConfigReaders {
  given ConfigReader[DateTime] =
    ConfigReader.fromNonEmptyString {
      stringValue =>
        Try(DateTime.parse(stringValue)).toEither.left.map {
          throwable => CannotConvert(stringValue, classOf[DateTime].getSimpleName, throwable.getMessage)
        }
    }

  given ConfigReader[Host] = ConfigReader.fromNonEmptyStringOpt(Host.fromString)

  given ConfigReader[Port] = ConfigReader.fromNonEmptyStringOpt(Port.fromString)

  given ConfigReader[Password] = ConfigReader.fromNonEmptyString(stringValue => Right(Password(stringValue)))
}