package com.ruchij.api.web.middleware

import cats.{ApplicativeError, MonadThrow}
import cats.data.{Kleisli, OptionT}
import cats.effect.kernel.Sync
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.exceptions.AuthenticationException
import com.ruchij.api.services.authentication.AuthenticationService
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret
import org.http4s.Credentials.Token
import org.http4s.{ContextRequest, Request, Response}
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware

object UserAuthenticator {
  val AuthenticationCookie = "authentication-cookie"

  def apply[F[_]: MonadThrow](authenticationService: AuthenticationService[F]): AuthMiddleware[F, User] =
    authenticatedRoutes =>
      Kleisli[[A] =>> OptionT[F, A], Request[F], Response[F]] { request =>
        OptionT
          .fromOption[F](token(request).orElse(authenticationCookie(request)))
          .orElseF {
            ApplicativeError[F, Throwable].raiseError {
              AuthenticationException("Authentication secret not found in Authorization header or cookie")
            }
          }
          .semiflatMap(authenticationService.authenticate)
          .flatMap { user =>
            authenticatedRoutes.run(ContextRequest[F, User](user, request))
          }
      }

  private def token(request: Request[_]): Option[AuthenticationSecret] =
    request.headers
      .get[Authorization]
      .map(_.credentials)
      .collect { case Token(_, token) => AuthenticationSecret(token) }

  private def authenticationCookie(request: Request[_]): Option[AuthenticationSecret] =
    request.cookies
      .find(_.name == AuthenticationCookie)
      .map(cookie => AuthenticationSecret(cookie.content))
}
