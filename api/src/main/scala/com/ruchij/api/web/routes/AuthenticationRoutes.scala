package com.ruchij.api.web.routes

import cats.effect.kernel.Async
import cats.implicits.*
import com.ruchij.api.circe.Decoders.given
import com.ruchij.api.circe.Encoders.given
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.services.authentication.AuthenticationService
import com.ruchij.api.web.middleware.UserAuthenticator
import com.ruchij.api.web.requests.UserLoginRequest
import com.ruchij.api.types.FunctionKTypes.{given, *}
import io.circe.generic.auto.*
import org.http4s.ContextRoutes
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.ResponseCookie

object AuthenticationRoutes {

  def apply[F[_]: Async](authenticationService: AuthenticationService[F])(using dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._  

    HttpRoutes.of[F] {
      case request @ POST -> Root / "login" =>
        for {
          userLoginRequest <- request.as[UserLoginRequest]
          authenticationToken <- authenticationService.login(userLoginRequest.email, userLoginRequest.password)
          
          cookie = 
            ResponseCookie(
              name = UserAuthenticator.AuthenticationCookieName, 
              content = authenticationToken.secret.toString(),
              path = Some("/")
            )
          
          response <- Created(authenticationToken)
        }
        yield response.addCookie(cookie)

      case request @ DELETE -> Root / "logout" =>
        for {
          authenticationSecret <- UserAuthenticator.authenticationSecret(request).toType[F, Throwable]
          user <- authenticationService.logout(authenticationSecret)
          response <- Ok(user)
        }
        yield response.removeCookie(UserAuthenticator.AuthenticationCookieName)

    } <+>
      UserAuthenticator[F](authenticationService).apply {
        ContextRoutes.of[User, F] {
          case GET -> Root / "user" as user => Ok(user)
        }
      }
  }
  
}
