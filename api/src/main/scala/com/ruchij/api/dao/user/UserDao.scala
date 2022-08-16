package com.ruchij.api.dao.user

import com.ruchij.api.dao.user.models.User
import com.ruchij.api.dao.user.models.Emails.Email

trait UserDao[F[_]] {
  def insert(user: User): F[Int]

  def findByEmail(email: Email): F[Option[User]]

  def findById(userId: String): F[Option[User]]
}
