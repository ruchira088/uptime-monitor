package com.ruchij.api.services.authentication

import com.ruchij.api.services.authentication.models.AuthenticationToken
import com.ruchij.api.services.authentication.models.AuthenticationSession
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.dao.user.models.User

trait AuthenticationService[F[_]] {
  def login(email: Email, password: Password): F[AuthenticationToken]

  def authenticate(authenticationSecret: AuthenticationSecret): F[AuthenticationSession]

  def logout(authenticationSecret: AuthenticationSecret): F[AuthenticationToken]
}
