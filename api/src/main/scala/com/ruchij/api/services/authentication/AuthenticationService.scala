package com.ruchij.api.services.authentication

import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.services.authentication.models.AuthenticationToken
import com.ruchij.api.services.authentication.models.AuthenticationToken.Secret

trait AuthenticationService[F[_]] {
  def login(email: Email, password: Password): F[AuthenticationToken]

  def authenticate(authenticationSecret: Secret): F[User]

  def logout(authenticationSecret: Secret): F[User]
}
