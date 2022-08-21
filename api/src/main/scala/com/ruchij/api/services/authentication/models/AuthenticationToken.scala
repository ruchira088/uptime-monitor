package com.ruchij.api.services.authentication.models

import com.ruchij.api.dao.models.IDs.ID
import org.joda.time.DateTime
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret

final case class AuthenticationToken(
  secret: AuthenticationSecret,
  createdAt: DateTime,
  updatedAt: DateTime,
  userId: ID[User],
  renewals: Long
)

object AuthenticationToken {
  opaque type AuthenticationSecret = String

  object AuthenticationSecret {
    def apply(authenticationSecret: String): AuthenticationSecret = authenticationSecret
  }
}
