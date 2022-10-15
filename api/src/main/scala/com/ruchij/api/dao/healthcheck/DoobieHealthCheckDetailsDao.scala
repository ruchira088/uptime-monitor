package com.ruchij.api.dao.healthcheck

import com.ruchij.api.dao.healthcheck.models.HealthCheckDetails
import com.ruchij.api.dao.doobie.DoobieCodecs.given
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import doobie.Fragments.{setOpt, whereAndOpt}
import doobie.util.fragment.Fragment
import cats.Applicative

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
    """.update.run

  override def findById(
    id: ID[HealthCheckDetails],
    maybeUserId: Option[ID[User]]
  ): ConnectionIO[Option[HealthCheckDetails]] =
    (SelectQuery ++ fr"WHERE id = $id" ++ maybeUserId.map(userId => fr"AND user_id = $userId").getOrElse(Fragment.empty))
      .query[HealthCheckDetails]
      .option

  override def findByUserId(id: ID[User]): ConnectionIO[Seq[HealthCheckDetails]] =
    (SelectQuery ++ fr"WHERE user_id = $id").query[HealthCheckDetails].to[Seq]

  override def update(
    id: ID[HealthCheckDetails],
    maybeName: Option[String],
    maybeDescription: Option[String],
    maybeUserId: Option[ID[User]]
  ): ConnectionIO[Int] = 
    if List(maybeName, maybeDescription).forall(_.isEmpty) 
    then Applicative[ConnectionIO].pure(0)
    else
      (fr"UPDATE health_check" ++ 
        setOpt(
          maybeName.map(name => fr"name = $name"), 
          maybeDescription.map(
            description => if description.isEmpty() then fr"description = NULL" else fr"description = $description"
            )
        ) ++ 
        whereAndOpt(Some(fr"id = $id"), maybeUserId.map(userId => fr"user_id = $userId"))
      )
          .update
          .run 
    
}
