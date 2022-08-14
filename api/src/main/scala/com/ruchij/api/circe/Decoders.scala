package com.ruchij.api.circe

import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import io.circe.Decoder
import org.joda.time.DateTime

import scala.util.Try

object Decoders {
  given Decoder[DateTime] =
    Decoder.decodeString.emapTry(dateTimeString => Try(DateTime.parse(dateTimeString)))
    
  given Decoder[Email] =
    Decoder.decodeString.emap(emailString => Email(emailString).left.map(_.message))
    
  given Decoder[Password] =
    Decoder.decodeString.map(password => Password(password))  
}
