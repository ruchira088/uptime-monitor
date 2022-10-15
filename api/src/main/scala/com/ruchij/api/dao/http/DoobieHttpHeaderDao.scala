package com.ruchij.api.dao.http

import com.ruchij.api.dao.http.models.{HttpEndpoint, HttpHeader}
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.doobie.DoobieCodecs.given
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import doobie.Fragments.setOpt
import com.ruchij.api.dao.user.models.User
import cats.Applicative

object DoobieHttpHeaderDao extends HttpHeaderDao[ConnectionIO] {
  private val SelectQuery = sql"SELECT id, created_at, http_endpoint_id, header_name, header_value FROM http_header"

  override def insert(httpHeader: HttpHeader): ConnectionIO[Int] =
    sql"""
      INSERT INTO http_header (id, created_at, http_endpoint_id, header_name, header_value)
        VALUES (
          ${httpHeader.id},
          ${httpHeader.createdAt},
          ${httpHeader.httpEndpointId},
          ${httpHeader.name},
          ${httpHeader.value}
        )
     """.update.run

  override def findById(id: ID[HttpHeader]): ConnectionIO[Option[HttpHeader]] =
    (SelectQuery ++ fr"WHERE id = $id")
      .query[HttpHeader]
      .option

  override def findByHttpEndpointId(id: ID[HttpEndpoint]): ConnectionIO[Seq[HttpHeader]] =
    (SelectQuery ++ fr"WHERE http_endpoint_id = $id")
      .query[HttpHeader]
      .to[Seq]

  override def findUserId(id: ID[HttpHeader]): ConnectionIO[Option[ID[User]]] =
    sql"""
      SELECT health_check.user_id FROM http_header 
        INNER JOIN health_check ON http_header.http_endpoint_id = health_check.http_endpoint_id
        WHERE http_header.id = $id
    """
      .query[ID[User]]
      .option

  override def update(id: ID[HttpHeader], maybeName: Option[String], maybeValue: Option[String]): ConnectionIO[Int] =
    if List(maybeName, maybeValue).forall(_.isEmpty) then Applicative[ConnectionIO].pure(0)
    else
      (fr"UPDATE http_header" ++
        setOpt(maybeName.map(name => fr"name = $name"), maybeValue.map(value => fr"value = $value")) ++
        fr"WHERE id = $id").update.run

}
