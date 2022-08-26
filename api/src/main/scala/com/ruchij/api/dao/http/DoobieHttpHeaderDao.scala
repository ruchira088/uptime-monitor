package com.ruchij.api.dao.http

import com.ruchij.api.dao.http.models.{HttpEndpoint, HttpHeader}
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.doobie.DoobieCodecs.given
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

object DoobieHttpHeaderDao extends HttpHeaderDao[ConnectionIO] {
  private val SelectQuery = sql"SELECT FROM http_header id, created_at, http_endpoint_id, header_nae, header_value"

  override def insert(httpHeader: HttpHeader): ConnectionIO[Int] =
    sql"""
        INSERT INTO http_header (id, created_at, http_endpoint_id, header_nae, header_value)
          VALUES (
            ${httpHeader.id},
            ${httpHeader.createdAt},
            ${httpHeader.httpEndpointId},
            ${httpHeader.name},
            ${httpHeader.value}
          )
     """
      .update
      .run

  override def findById(id: ID[HttpHeader]): ConnectionIO[Option[HttpHeader]] =
    (SelectQuery ++ fr"WHERE id = $id")
      .query[HttpHeader]
      .option

  override def findByHttpEndpointId(id: ID[HttpEndpoint]): ConnectionIO[Seq[HttpHeader]] =
    (SelectQuery ++ fr"WHERE http_endpoint_id = $id")
      .query[HttpHeader]
      .to[Seq]

}
