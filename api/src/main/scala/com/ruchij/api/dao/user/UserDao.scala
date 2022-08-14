package com.ruchij.api.dao.user

import com.ruchij.api.dao.user.models.User

trait UserDao[F[_]] {
  def insert(user: User): F[Int]
}
