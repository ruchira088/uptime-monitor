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
import com.ruchij.api.services.authentication.models.AuthenticationToken
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret
import com.ruchij.api.services.hash.PasswordHashingService
import com.ruchij.api.types.IdGenerator.IdPrefix
import com.ruchij.api.types.{IdGenerator, JodaClock}
import io.circe.generic.auto.*

import scala.concurrent.duration.FiniteDuration

class AuthenticationServiceImpl[F[_]: MonadThrow: JodaClock: IdGenerator, G[_]: MonadThrow](
  keyValueStore: KeyValueStore[F],
  passwordHashingService: PasswordHashingService[F],
  userDao: UserDao[G],
  credentialsDao: CredentialsDao[G],
  authenticationConfiguration: AuthenticationConfiguration
)(using transaction: G ~> F) extends AuthenticationService[F] {

  private val keyspacedKeyValueStore: KeyspacedKeyValueStore[F, AuthenticationSecret, AuthenticationToken] =
    KeyspacedKeyValueStore[F, AuthenticationSecret, AuthenticationToken](keyValueStore)

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
          IdGenerator[F].generate[AuthenticationSecret].map { authenticationSecret =>
            AuthenticationToken(
              secret = AuthenticationSecret(authenticationSecret.toString),
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

  override def authenticate(authenticationSecret: AuthenticationSecret): F[User] =
    OptionT(keyspacedKeyValueStore.get(authenticationSecret))
      .getOrRaise(AuthenticationException("Authentication token not found"))
      .flatMap { authenticationToken =>
        JodaClock[F].timestamp
          .map { timestamp =>
            authenticationToken.copy(expiresAt = timestamp.plus(authenticationConfiguration.sessionDuration.toMillis))
          }  
      }
      .flatTap { updatedAuthenticationToken =>
        keyspacedKeyValueStore.put(
          authenticationSecret, 
          updatedAuthenticationToken, 
          Some(authenticationConfiguration.sessionDuration)
        )  
      }
      .flatMap { updatedAuthenticationToken =>
        OptionT(transaction(userDao.findById(updatedAuthenticationToken.userId)))
          .getOrRaise(ResourceNotFoundException(s"User not found with id=${updatedAuthenticationToken.userId}"))
      }

  override def logout(authenticationSecret: AuthenticationSecret): F[User] = ???

}

object AuthenticationServiceImpl {
  given Keyspace[AuthenticationSecret, AuthenticationToken] =
    Keyspace[AuthenticationSecret, AuthenticationToken](AuthenticationSecret("authentication-"))

  given IdPrefix[AuthenticationSecret] with { override val value: String = "" }
}
