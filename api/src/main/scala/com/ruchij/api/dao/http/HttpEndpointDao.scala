package com.ruchij.api.dao.http

import com.ruchij.api.dao.models.IDs.ID

trait HttpEndpointDao[F[_]] {
  def insert(httpEndpoint: HttpEndpoint): F[Int]

  def findById(id: ID[HttpEndpoint]): F[Option[HttpEndpoint]]
}
