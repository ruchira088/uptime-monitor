package com.ruchij.api.services.authentication

import cats.data.OptionT
import cats.implicits.*
import cats.{Applicative, ApplicativeError, MonadThrow, Semigroup, ~>}
import com.ruchij.api.circe.Decoders.given
import com.ruchij.api.circe.Encoders.given
import com.ruchij.api.config.AuthenticationConfiguration
import com.ruchij.api.dao.credentials.CredentialsDao
import com.ruchij.api.dao.user.UserDao
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.exceptions.{AuthenticationException, ResourceNotFoundException}
import com.ruchij.api.kvstore.{KeyValueStore, Keyspace, KeyspacedKeyValueStore}
import com.ruchij.api.services.authentication.AuthenticationServiceImpl.given
import com.ruchij.api.services.authentication.AuthenticationServiceImpl.Session
import com.ruchij.api.services.authentication.models.AuthenticationToken
import com.ruchij.api.services.authentication.models.AuthenticationToken.{Secret, SecretGenerator}
import com.ruchij.api.services.hash.PasswordHashingService
import com.ruchij.api.types.IdGenerator.IdPrefix
import com.ruchij.api.types.{IdGenerator, JodaClock}
import com.ruchij.api.types.RandomGenerator
import io.circe.generic.auto.*

import scala.concurrent.duration.FiniteDuration
import cats.syntax.validated

class AuthenticationServiceImpl[F[_]: MonadThrow: JodaClock: SecretGenerator, G[_]: MonadThrow](
  keyValueStore: KeyValueStore[F],
  passwordHashingService: PasswordHashingService[F],
  userDao: UserDao[G],
  credentialsDao: CredentialsDao[G],
  authenticationConfiguration: AuthenticationConfiguration
)(using transaction: G ~> F) extends AuthenticationService[F] {

  private val keyspacedKeyValueStore: KeyspacedKeyValueStore[F, Secret, AuthenticationToken] =
    KeyspacedKeyValueStore[F, Secret, AuthenticationToken](keyValueStore)

  override def login(email: Email, password: Password): F[AuthenticationToken] =
    transaction {
      OptionT(userDao.findByEmail(email))
        .flatMapF(user => credentialsDao.findByUserId(user.id))
        .getOrRaise(ResourceNotFoundException(s"User not found with email=$email"))
    }
      .flatMap { credentials =>
        passwordHashingService.checkPassword(password, credentials.hashedPassword)
          .flatMap { isMatch =>
            if isMatch then Applicative[F].pure(credentials)
            else ApplicativeError[F, Throwable].raiseError(AuthenticationException("Password incorrect"))
          }
      }
      .flatMap { credentials =>
        JodaClock[F].timestamp.flatMap { timestamp =>
          RandomGenerator[F, Secret].generate.map { authenticationSecret =>
            AuthenticationToken(
              secret = Secret(authenticationSecret.toString),
              createdAt = timestamp,
              updatedAt = timestamp,
              userId = credentials.userId,
              renewals = 0,
              expiresAt = timestamp.plus(authenticationConfiguration.sessionDuration.toMillis)
            )
          }
        }
      }
      .flatTap { authenticationToken =>
        keyspacedKeyValueStore.put(
          authenticationToken.secret, 
          authenticationToken, 
          Some(authenticationConfiguration.sessionDuration)
        )  
      }

  override def authenticate(authenticationSecret: Secret): F[User] =
    for {
      session <- validate(authenticationSecret)
      timestamp <- JodaClock[F].timestamp

      token = 
        session.authenticationToken
          .copy(
            expiresAt = timestamp.plus(authenticationConfiguration.sessionDuration.toMillis),
            renewals = session.authenticationToken.renewals + 1
          )

      _ <- keyspacedKeyValueStore.put(authenticationSecret, token, Some(authenticationConfiguration.sessionDuration))
    } yield session.user

  override def logout(authenticationSecret: Secret): F[User] =
    validate(authenticationSecret)
      .flatMap { session =>
        keyspacedKeyValueStore.delete(authenticationSecret).as(session.user)  
      }
  
  private def validate(authenticationSecret: Secret): F[Session] =
    OptionT(keyspacedKeyValueStore.get(authenticationSecret))
      .getOrRaise(AuthenticationException("Authentication token not found"))
      .flatMap { authenticationToken =>
        JodaClock[F].timestamp
          .flatMap { timestamp =>
            if authenticationToken.expiresAt.isAfter(timestamp) 
            then Applicative[F].pure(authenticationToken)
            else ApplicativeError[F, Throwable].raiseError {
              AuthenticationException("Authentication token is expired")
            }
          }  
      }
      .flatMap { authenticationToken =>
        OptionT(transaction(userDao.findById(authenticationToken.userId)))
          .getOrRaise(ResourceNotFoundException(s"User not found with id=${authenticationToken.userId}"))
          .map(user => Session(user, authenticationToken))
      }

}

object AuthenticationServiceImpl {
  private final case class Session(user: User, authenticationToken: AuthenticationToken)

  given Keyspace[Secret, AuthenticationToken] =
    Keyspace[Secret, AuthenticationToken](Secret("authentication-"))

  given IdPrefix[Secret] with { override val value: String = "" }
}
