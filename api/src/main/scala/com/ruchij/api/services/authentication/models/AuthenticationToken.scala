package com.ruchij.api.services.authentication.models

import cats.Semigroup
import cats.kernel.instances.StringMonoid
import com.ruchij.api.dao.models.IDs.ID
import org.joda.time.DateTime
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.services.authentication.models.AuthenticationToken.AuthenticationSecret

final case class AuthenticationToken(
  secret: AuthenticationSecret,
  createdAt: DateTime,
  updatedAt: DateTime,
  userId: ID[User],
  renewals: Long,
  expiresAt: DateTime
)

object AuthenticationToken {
  opaque type AuthenticationSecret = String

  given Semigroup[AuthenticationSecret] = new StringMonoid

  object AuthenticationSecret {
    def apply(authenticationSecret: String): AuthenticationSecret = authenticationSecret
  }
}
