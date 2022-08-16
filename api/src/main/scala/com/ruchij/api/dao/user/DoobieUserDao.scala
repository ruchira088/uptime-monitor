package com.ruchij.api.dao.user

import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import com.ruchij.api.dao.doobie.DoobieCodecs.given
import com.ruchij.api.dao.user.models.User

object DoobieUserDao extends UserDao[ConnectionIO] {

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

}
