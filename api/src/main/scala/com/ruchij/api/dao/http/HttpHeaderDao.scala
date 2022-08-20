package com.ruchij.api.dao.http

import com.ruchij.api.dao.http.models.HttpHeader
import com.ruchij.api.dao.models.IDs.ID

trait HttpHeaderDao[F[_]] {
  def insert(httpHeader: HttpHeader): F[Int]

  def findById(id: ID[HttpHeader]): F[Option[HttpHeader]]

  def findByHttpEndpointId(id: ID[HttpEndpoint]): F[Seq[HttpHeader]]
}