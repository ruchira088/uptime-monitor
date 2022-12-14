package com.ruchij.api.circe

import com.ruchij.api.circe.Encoders.given_Encoder_DateTime
import io.circe.{Encoder, Json}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class CirceEncodersSpec extends AnyFlatSpec with Matchers {

  "dateTimeEncoder" should "encode DateTime" in {
    val dateTime = DateTime(2021, 9, 12, 19, 1, 49, 100, DateTimeZone.UTC)

    Encoder[DateTime].apply(dateTime) mustBe Json.fromString("2021-09-12T19:01:49.100Z")
  }

}
