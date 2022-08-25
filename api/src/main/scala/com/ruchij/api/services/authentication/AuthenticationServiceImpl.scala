package com.ruchij.api.services.authentication

import cats.Semigroup
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.kvstore.{KeyValueStore, Keyspace}
import com.ruchij.api.services.authentication.models.AuthenticationToken
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret
import com.ruchij.api.circe.Decoders.given
import com.ruchij.api.circe.Encoders.given
import io.circe.generic.auto.*

class AuthenticationServiceImpl[F[_], G[_]](keyValueStore: KeyValueStore[F]) extends AuthenticationService[F] {

  override def login(email: Email, password: Password): F[AuthenticationToken] = ???

  override def authenticate(authenticationSecret: AuthenticationSecret): F[User] = ???

  override def logout(authenticationSecret: AuthenticationSecret): F[User] = ???

}

object AuthenticationServiceImpl {
  given Keyspace[AuthenticationSecret, AuthenticationToken] =
    Keyspace[AuthenticationSecret, AuthenticationToken](AuthenticationSecret("authentication-"))
}
