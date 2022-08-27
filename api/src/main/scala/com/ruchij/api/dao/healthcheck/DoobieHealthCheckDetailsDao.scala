package com.ruchij.api.dao.healthcheck

import com.ruchij.api.dao.healthcheck.models.HealthCheckDetails
import com.ruchij.api.dao.doobie.DoobieCodecs.given
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import doobie.Fragments.orOpt

object DoobieHealthCheckDetailsDao extends HealthCheckDetailsDao[ConnectionIO] {
  private val SelectQuery = sql"SELECT id, name, description, created_at, user_id, http_endpoint_id FROM health_check"

  override def insert(healthCheckDetails: HealthCheckDetails): ConnectionIO[Int] =
    sql"""
        INSERT INTO health_check (id, name, description, created_at, user_id, http_endpoint_id)
          VALUES (
            ${healthCheckDetails.id},
            ${healthCheckDetails.createdAt},
            ${healthCheckDetails.name},
            ${healthCheckDetails.description},
            ${healthCheckDetails.userId},
            ${healthCheckDetails.httpEndpointId}
          )
    """
      .update
      .run

  override def findById(id: ID[HealthCheckDetails], maybeUserId: Option[ID[User]]): ConnectionIO[Option[HealthCheckDetails]] =
    (SelectQuery ++ fr"WHERE id = $id" ++ orOpt(maybeUserId.map(userId => fr"user_id = $userId"))).query[HealthCheckDetails].option

  override def findByUserId(id: ID[User]): ConnectionIO[Seq[HealthCheckDetails]] =
    (SelectQuery ++ fr"WHERE user_id = $id").query[HealthCheckDetails].to[Seq]
}
