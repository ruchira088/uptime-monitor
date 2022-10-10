package com.ruchij.api.services.authentication.models

import cats.Semigroup
import cats.effect.kernel.Sync
import cats.implicits.*
import cats.kernel.instances.StringMonoid
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.services.authentication.models.AuthenticationToken.Secret
import com.ruchij.api.types.RandomGenerator
import org.joda.time.DateTime
import java.util.UUID

final case class AuthenticationToken(
  secret: Secret,
  createdAt: DateTime,
  updatedAt: DateTime,
  userId: ID[User],
  renewals: Long,
  expiresAt: DateTime
)

object AuthenticationToken {
  opaque type Secret = String

  type SecretGenerator[F[_]] = RandomGenerator[F, Secret]

  given Semigroup[Secret] = new StringMonoid

  given [F[_]: Sync]: RandomGenerator[F, Secret] =
    RandomGenerator[F, UUID].map(uuid => Secret(uuid.toString()))

  object Secret {
    def apply(authenticationSecret: String): Secret = authenticationSecret
  }
}
