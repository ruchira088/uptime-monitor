package com.ruchij.api.dao.credentials

import com.ruchij.api.dao.credentials.models.Credentials

trait CredentialsDao[F[_]] {
  def insert(credentials: Credentials): F[Int]
}
