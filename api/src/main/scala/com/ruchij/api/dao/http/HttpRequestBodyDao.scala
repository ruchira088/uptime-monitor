package com.ruchij.api.dao.http

import com.ruchij.api.dao.http.models.HttpRequestBody
import com.ruchij.api.dao.http.models.HttpEndpoint
import com.ruchij.api.dao.models.IDs.ID

trait HttpRequestBodyDao[F[_]] {
  def insert(httpRequestBody: HttpRequestBody): F[Int]

  def findById(id: ID[HttpRequestBody]): F[Option[HttpRequestBody]]

  def findByHttpEndpointId(id: ID[HttpEndpoint]): F[Option[HttpRequestBody]]
}
