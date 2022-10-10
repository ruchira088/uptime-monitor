package com.ruchij.api.web.middleware

import cats.{ApplicativeError, MonadThrow}
import cats.data.{Kleisli, OptionT}
import cats.effect.kernel.Sync
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.exceptions.AuthenticationException
import com.ruchij.api.services.authentication.AuthenticationService
import com.ruchij.api.services.authentication.models.AuthenticationToken.Secret
import com.ruchij.api.types.FunctionKTypes.{given, *}
import org.http4s.Credentials.Token
import org.http4s.{ContextRequest, Request, Response}
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware

object UserAuthenticator {
  val AuthenticationCookieName = "authentication-cookie"

  def apply[F[_]: MonadThrow](authenticationService: AuthenticationService[F]): AuthMiddleware[F, User] =
    authenticatedRoutes =>
      Kleisli[[A] =>> OptionT[F, A], Request[F], Response[F]] { request =>
        OptionT.liftF(authenticationSecret(request).toType[F, Throwable])
          .semiflatMap(authenticationService.authenticate)
          .flatMap { user =>
            authenticatedRoutes.run(ContextRequest[F, User](user, request))
          }
      }

  def authenticationSecret(request: Request[_]): Either[AuthenticationException, Secret] =
    token(request).orElse(authenticationCookie(request))
      .toRight(AuthenticationException("Authentication secret not found in Authorization header or cookie"))

  private def token(request: Request[_]): Option[Secret] =
    request.headers
      .get[Authorization]
      .map(_.credentials)
      .collect { case Token(_, token) => Secret(token) }

  private def authenticationCookie(request: Request[_]): Option[Secret] =
    request.cookies
      .find(_.name == AuthenticationCookieName)
      .map(cookie => Secret(cookie.content))
}
