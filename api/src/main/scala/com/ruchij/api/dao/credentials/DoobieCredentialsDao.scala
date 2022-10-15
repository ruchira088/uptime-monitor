package com.ruchij.api.dao.credentials

import com.ruchij.api.dao.credentials.models.Credentials
import com.ruchij.api.dao.doobie.DoobieCodecs.given
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

object DoobieCredentialsDao extends CredentialsDao[ConnectionIO] {

  override def insert(credentials: Credentials): ConnectionIO[Int] =
    sql"""
      INSERT INTO user_credentials (user_id, created_at, hashed_password)
        VALUES(${credentials.userId}, ${credentials.createdAt}, ${credentials.hashedPassword})
    """
      .update
      .run

  override def findByUserId(userId: ID[User]): ConnectionIO[Option[Credentials]] =
    sql"SELECT user_id, created_at, hashed_password FROM user_credentials WHERE user_id = $userId"
      .query[Credentials]
      .option
}
