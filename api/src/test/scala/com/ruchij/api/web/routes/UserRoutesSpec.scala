package com.ruchij.api.web.routes

import cats.effect.IO
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.test.matchers.*
import com.ruchij.api.test.mixins.EitherValues
import com.ruchij.api.test.mixins.io.MockedRoutesIO
import com.ruchij.api.test.utils.IOUtils.runIO
import io.circe.literal.json
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.client.dsl.io.*
import org.http4s.dsl.io.POST
import org.http4s.syntax.all.uri
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class UserRoutesSpec extends AnyFlatSpec with Matchers with MockedRoutesIO with EitherValues {
  
  "POST /user" should "create a new user" in runIO {
    val requestBody =
      json"""{
        "email": "me@ruchij.com",
        "password": "MyPassword",
        "firstName": "Ruchira",
        "lastName": "Jayasekara"
      }"""

    val user = 
      User(
        id = ID.create[User]("my-id"),
        createdAt = new DateTime(2021, 8, 4, 10, 10, 0, 0, DateTimeZone.UTC),
        email = Email("me@ruchij.com").value,
        firstName = "Ruchira",
        lastName = "Jayasekara"
      )

    val expectedJsonResponse =
      json"""{
        "id": "user-my-id",
        "createdAt": "2021-08-04T10:10:00.000Z",
        "email": "me@ruchij.com",
        "firstName": "Ruchira",
        "lastName": "Jayasekara"
      }"""

    for {
      _ <- IO.delay {
        when {
          userService.create(
            any(),
            any(), 
            any(), 
            any()
          ) 
        }
          .thenReturn(IO.pure(user))
        }

      routes = createRoutes

      response <- routes(POST(requestBody, uri"/user"))

      _ <- IO.delay {
        response must beJsonContentType
        response must haveJson(expectedJsonResponse)
        response must haveStatus(Status.Created)
      }
    }
    yield (): Unit
  }

}
