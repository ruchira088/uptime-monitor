package com.ruchij.api.services.user

import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.dao.user.models.User

trait UserService[F[_]] {
  def create(email: Email, password: Password, firstName: String, lastName: String): F[User]
}
