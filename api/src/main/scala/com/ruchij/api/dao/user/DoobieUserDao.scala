package com.ruchij.api.dao.user

import com.ruchij.api.dao.doobie.DoobieCodecs.given
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.User
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import com.ruchij.api.dao.models.IDs.ID

object DoobieUserDao extends UserDao[ConnectionIO] {

  private val SelectQuery =
    fr"SELECT id, created_at, email, first_name, last_name FROM api_user"

  override def insert(user: User): ConnectionIO[Int] =
    sql"""
      INSERT INTO api_user (id, created_at, email, first_name, last_name)
          VALUES (
              ${user.id},
              ${user.createdAt},
              ${user.email},
              ${user.firstName},
              ${user.lastName}
          )
    """
        .update
        .run

  override def findByEmail(email: Email): ConnectionIO[Option[User]] =
    (SelectQuery ++ fr"WHERE email = $email").query[User].option

  override def findById(userId: ID[User]): ConnectionIO[Option[User]] =
    (SelectQuery ++ fr"WHERE id = $userId").query[User].option

}
