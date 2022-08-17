package com.ruchij.api.services.hash

import cats.effect.kernel.Sync
import cats.implicits.*
import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword
import com.ruchij.api.dao.user.models.Passwords.Password
import org.mindrot.jbcrypt.BCrypt

class BCryptPasswordHashingService[F[_]: Sync] extends PasswordHashingService[F] {

  override def hash(password: Password): F[HashedPassword] =
    Sync[F].delay(BCrypt.hashpw(password.toString, BCrypt.gensalt()))
        .map(HashedPassword.apply)

  override def checkPassword(password: Password, hashedPassword: HashedPassword): F[Boolean] =
    Sync[F].delay(BCrypt.checkpw(password.toString, hashedPassword.toString))

}
