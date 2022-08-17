package com.ruchij.api.services.hash

import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword
import com.ruchij.api.dao.user.models.Passwords.Password

trait PasswordHashingService[F[_]] {
    def hash(password: Password): F[HashedPassword]

    def checkPassword(password: Password, hashedPassword: HashedPassword): F[Boolean]
}
