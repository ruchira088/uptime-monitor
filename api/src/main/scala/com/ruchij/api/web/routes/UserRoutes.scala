package com.ruchij.api.web.routes

import cats.effect.Async
import cats.implicits.*
import com.ruchij.api.web.requests.{CreateUserRequest, to}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec.*
import io.circe.generic.auto.*
import com.ruchij.api.circe.Decoders.given
import com.ruchij.api.circe.Encoders.given
import com.ruchij.api.services.user.UserService

object UserRoutes {
  def apply[F[_]: Async](userService: UserService[F])(using dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    HttpRoutes.of[F] {
      case request @ POST -> Root =>
        for {
          createUserRequest <- request.to[CreateUserRequest]
          user <- userService.create(createUserRequest.email, createUserRequest.password, createUserRequest.firstName, createUserRequest.lastName)
          response <- Created(user)
        }
        yield response
    }
  }

}
