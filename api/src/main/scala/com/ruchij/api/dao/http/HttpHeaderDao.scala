package com.ruchij.api.dao.http

import com.ruchij.api.dao.http.models.HttpHeader
import com.ruchij.api.dao.http.models.HttpEndpoint
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User

trait HttpHeaderDao[F[_]] {
  def insert(httpHeader: HttpHeader): F[Int]

  def findById(id: ID[HttpHeader]): F[Option[HttpHeader]]

  def findByHttpEndpointId(id: ID[HttpEndpoint]): F[Seq[HttpHeader]]

  def findUserId(id: ID[HttpHeader]): F[Option[ID[User]]]

  def update(id: ID[HttpHeader], maybeName: Option[String], maybeValue: Option[String]): F[Int]
}
