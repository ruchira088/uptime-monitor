package com.ruchij.api.web.middleware

import cats.Show
import cats.arrow.FunctionK
import cats.data.{Kleisli, NonEmptyList}
import cats.effect.Sync
import cats.implicits.*
import com.ruchij.api.exceptions.{AuthenticationException, ResourceConflictException, ResourceNotFoundException, ValidationException}
import com.ruchij.api.types.Logger
import com.ruchij.api.web.responses.ErrorResponse
import io.circe.DecodingFailure
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.impl.EntityResponseGenerator
import org.http4s.{HttpApp, Request, Response, Status}

object ExceptionHandler {
  private val logger = Logger[ExceptionHandler.type]

  def apply[F[_]: Sync](httpApp: HttpApp[F]): HttpApp[F] =
    Kleisli[F, Request[F], Response[F]] { request =>
      Sync[F].handleErrorWith(httpApp.run(request)) { throwable =>
        entityResponseGenerator[F](throwable)(throwableResponseBody(throwable))
          .map(errorResponseMapper(throwable))
          .flatTap(response => errorLogger(response, throwable))
      }
    }

  private val throwableStatusMapper: Throwable => Status = {
    case _: ResourceNotFoundException => Status.NotFound

    case _: ResourceConflictException => Status.Conflict

    case _: AuthenticationException => Status.Unauthorized

    case _: ValidationException => Status.BadRequest

    case _ => Status.InternalServerError
  }

  private val throwableResponseBody: Throwable => ErrorResponse = {
    case decodingFailure: DecodingFailure =>
      ErrorResponse {
        NonEmptyList.one {
          Show[DecodingFailure].show(decodingFailure)
        }
      }

    case throwable =>
      Option(throwable.getCause).fold(ErrorResponse(NonEmptyList.of(throwable.getMessage)))(throwableResponseBody)
  }

  private def errorLogger[F[_]: Sync](response: Response[F], throwable: Throwable): F[Unit] =
    if (response.status >= Status.InternalServerError)
      logger.error(s"${response.status.code} status error code was returned.", throwable)
    else logger.warn(throwable.getMessage)

  private def errorResponseMapper[F[_]](throwable: Throwable)(response: Response[F]): Response[F] =
    throwable match {
      case _: AuthenticationException => response.removeCookie(UserAuthenticator.AuthenticationCookieName)

      case _ => response
    }

  private def entityResponseGenerator[F[_]](throwable: Throwable): EntityResponseGenerator[F, F] =
    new EntityResponseGenerator[F, F] {
      override def status: Status = throwableStatusMapper(throwable)

      override def liftG: FunctionK[F, F] = FunctionK.id[F]
    }
}
