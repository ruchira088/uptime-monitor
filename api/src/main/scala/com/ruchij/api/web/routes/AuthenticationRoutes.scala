package com.ruchij.api.web.routes

import cats.effect.kernel.Async
import cats.implicits.*
import com.ruchij.api.circe.Decoders.given
import com.ruchij.api.circe.Encoders.given
import com.ruchij.api.services.authentication.AuthenticationService
import com.ruchij.api.services.authentication.models.AuthenticationSession
import com.ruchij.api.web.middleware.UserAuthenticator
import com.ruchij.api.web.requests.UserLoginRequest
import io.circe.generic.auto.*
import org.http4s.ContextRoutes
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl

object AuthenticationRoutes {

  def apply[F[_]: Async](authenticationService: AuthenticationService[F])(using dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._  

    HttpRoutes.of[F] {
      case request @ GET -> Root / "login" =>
        for {
          userLoginRequest <- request.as[UserLoginRequest]
          authenticationToken <- authenticationService.login(userLoginRequest.email, userLoginRequest.password)
          response <- Created(authenticationToken)
        }
        yield response.addCookie(UserAuthenticator.AuthenticationCookie, authenticationToken.secret.toString)
    } <+>
      UserAuthenticator[F](authenticationService).apply {
        ContextRoutes.of[AuthenticationSession, F] {
          case GET -> Root / "user" as authenticationSession => Ok(authenticationSession.user)

          case DELETE -> Root / "logout" as authenticationSession =>
            authenticationService.logout(authenticationSession.authenticationToken.secret)
              .productR(Ok(authenticationSession.user))
        }
      }
  }
  
}
