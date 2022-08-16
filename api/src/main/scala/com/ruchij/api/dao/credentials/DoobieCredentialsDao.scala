package com.ruchij.api.dao.credentials

import com.ruchij.api.dao.credentials.models.Credentials
import com.ruchij.api.dao.doobie.DoobieCodecs.given
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

}
