package com.ruchij.api.dao.credentials

import com.ruchij.api.dao.credentials.models.Credentials
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User

trait CredentialsDao[F[_]] {
  def insert(credentials: Credentials): F[Int]
  
  def findByUserId(userId: ID[User]): F[Option[Credentials]]
}
