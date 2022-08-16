package com.ruchij.api.services.user

import cats.~>
import cats.implicits.*
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password
import com.ruchij.api.dao.user.models.User
import com.ruchij.api.dao.user.UserDao
import cats.effect.kernel.Sync
import cats.Applicative
import cats.ApplicativeError
import com.ruchij.api.exceptions.ResourceConflictException
import com.ruchij.api.services.hash.PasswordHashingService
import com.ruchij.api.dao.credentials.CredentialsDao
import com.ruchij.api.types.JodaClock.apply
import com.ruchij.api.types.JodaClock
import com.ruchij.api.dao.credentials.models.Credentials
import cats.Monad
import com.ruchij.api.types.IdGenerator

class UserServiceImpl[F[_]: Sync: JodaClock: [K[_]] =>> IdGenerator[K, User], G[_]: Monad](passwordHashingService: PasswordHashingService[F], userDao: UserDao[G], credentialsDao: CredentialsDao[G])(using transaction: G ~> F) extends UserService[F] {

  override def create(email: Email, password: Password, firstName: String, lastName: String): F[User] =
    for {
        maybeExistingUser <- transaction(userDao.findByEmail(email))
        _ <- 
            if maybeExistingUser.isEmpty then Applicative[F].unit 
            else ApplicativeError[F, Throwable].raiseError {
                ResourceConflictException(s"User email=$email already exists")
            }

        hashedPassword <- passwordHashingService.hash(password)
        timestamp <- JodaClock[F].timestamp

        userId <- IdGenerator[F, User].generate
        user = User(id = userId, createdAt = timestamp, email = email, firstName = firstName, lastName = lastName)
        credentials = Credentials(userId = userId, createdAt = timestamp, hashedPassword = hashedPassword)

        _ <- transaction(userDao.insert(user).product(credentialsDao.insert(credentials)))

    } yield user

}
