package com.ruchij.api.dao.healthcheck

import com.ruchij.api.dao.healthcheck.models.HealthCheck
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User

trait HealthCheckDao[F[_]] {
  def insert(healthCheck: HealthCheck): F[Int]

  def findById(id: ID[HealthCheck]): F[Option[HealthCheck]]

  def findByUserId(id: ID[User]): F[Seq[HealthCheck]]
}