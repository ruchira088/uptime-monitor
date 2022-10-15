package com.ruchij.api.dao.http

import com.ruchij.api.dao.http.models.HttpEndpoint
import com.ruchij.api.dao.models.IDs.ID
import doobie.implicits.toSqlInterpolator
import doobie.ConnectionIO
import com.ruchij.api.dao.doobie.DoobieCodecs.given

object DoobieHttpEndpointDao extends HttpEndpointDao[ConnectionIO]  {
  override def insert(httpEndpoint: HttpEndpoint): ConnectionIO[Int] =
    sql"""
      INSERT INTO http_endpoint (id, created_at, updated_at, http_method, url)
        VALUES (
          ${httpEndpoint.id},
          ${httpEndpoint.createdAt},
          ${httpEndpoint.updatedAt},
          ${httpEndpoint.method},
          ${httpEndpoint.url}
        )
     """
      .update
      .run

  override def findById(id: ID[HttpEndpoint]): ConnectionIO[Option[HttpEndpoint]] =
    sql"SELECT id, created_at, updated_at, http_method, url FROM http_endpoint WHERE id = $id"
      .query[HttpEndpoint]
      .option
}
