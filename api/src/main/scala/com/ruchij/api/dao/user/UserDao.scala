package com.ruchij.api.dao.user

import com.ruchij.api.dao.user.models.User
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.models.IDs.ID

trait UserDao[F[_]] {
  def insert(user: User): F[Int]

  def findByEmail(email: Email): F[Option[User]]

  def findById(userId: ID[User]): F[Option[User]]
}
