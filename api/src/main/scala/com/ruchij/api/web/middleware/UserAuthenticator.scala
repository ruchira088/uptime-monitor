package com.ruchij.api.web.middleware

import cats.ApplicativeError
import cats.MonadThrow
import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.kernel.Sync
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.exceptions.AuthenticationException
import com.ruchij.api.services.authentication.AuthenticationService
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret
import com.ruchij.api.services.authentication.models.AuthenticationSession
import org.http4s.ContextRequest
import org.http4s.Credentials.Token
import org.http4s.Request
import org.http4s.Response
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware

object UserAuthenticator {
  val AuthenticationCookie = "authentication-cookie"

  def apply[F[_]: MonadThrow](authenticationService: AuthenticationService[F]): AuthMiddleware[F, AuthenticationSession] =
    authenticatedRoutes =>
      Kleisli[[A] =>> OptionT[F, A], Request[F], Response[F]] { request => 
        OptionT.fromOption[F](token(request).orElse(authenticationCookie(request)))
          .orElseF { 
            ApplicativeError[F, Throwable].raiseError {
              AuthenticationException("Authentication secret not found in Authorization header or cookie")
            }
          }
          .semiflatMap(authenticationService.authenticate)
          .flatMap { authenticationSession => 
            authenticatedRoutes.run(ContextRequest[F, AuthenticationSession](authenticationSession, request)) 
          }
      }

  private def token(request: Request[_]): Option[AuthenticationSecret] =
    request.headers.get[Authorization]
      .map(_.credentials)
      .collect { case Token(_, token) => AuthenticationSecret(token) }

  private def authenticationCookie(request: Request[_]): Option[AuthenticationSecret] =
    request.cookies
      .find(_.name == AuthenticationCookie)
      .map(cookie => AuthenticationSecret(cookie.content))
}
