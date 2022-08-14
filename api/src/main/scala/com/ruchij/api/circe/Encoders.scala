package com.ruchij.api.circe

import com.ruchij.api.dao.user.models.Emails.Email
import io.circe.Encoder
import org.joda.time.DateTime

object Encoders {
  given Encoder[DateTime] = Encoder.encodeString.contramap[DateTime](_.toString)

  given Encoder[Email] = Encoder.encodeString.contramap(_.toString)
}
