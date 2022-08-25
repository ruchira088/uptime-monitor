package com.ruchij.api.web.routes

import cats.effect.Async
import cats.implicits.*
import com.ruchij.api.circe.Decoders.given
import com.ruchij.api.circe.Encoders.given
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.services.user.UserService
import com.ruchij.api.web.requests.CreateUserRequest
import com.ruchij.api.web.requests.to
import io.circe.generic.auto.*
import org.http4s.ContextRoutes
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl

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
