package com.ruchij.api.services.hash

import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword

trait PasswordHashingService[F[_]] {
    def hash(password: Password): F[HashedPassword]

    def checkPassword(password: Password, hashedPassword: HashedPassword): F[Boolean]
}
