package com.ruchij.api.dao.http

import com.ruchij.api.dao.http.models.{HttpEndpoint, HttpRequestBody}
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.doobie.DoobieCodecs.given
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

object DoobieHttpRequestBodyDao extends HttpRequestBodyDao[ConnectionIO] {
  private val SelectQuery = sql"SELECT id, created_at, http_endpoint_id, content_type, body FROM http_request_body"

  override def insert(httpRequestBody: HttpRequestBody): ConnectionIO[Int] =
    sql"""
      INSERT INTO http_request_body (id, created_at, http_endpoint_id, content_type, body)
        VALUES (
          ${httpRequestBody.id},
          ${httpRequestBody.createdAt},
          ${httpRequestBody.httpEndpointId},
          ${httpRequestBody.contentType},
          ${httpRequestBody.body}
        )
    """
      .update
      .run

  override def findById(id: ID[HttpRequestBody]): ConnectionIO[Option[HttpRequestBody]] =
    (SelectQuery ++ fr"WHERE id = $id").query[HttpRequestBody].option

  override def findByHttpEndpointId(id: ID[HttpEndpoint]): ConnectionIO[Option[HttpRequestBody]] =
    (SelectQuery ++ fr"WHERE http_endpoint_id = $id").query[HttpRequestBody].option
}
