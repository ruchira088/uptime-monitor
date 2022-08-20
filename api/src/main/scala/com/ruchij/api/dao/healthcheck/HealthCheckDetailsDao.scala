package com.ruchij.api.dao.healthcheck

import com.ruchij.api.dao.healthcheck.models.HealthCheckDetails
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User

trait HealthCheckDetailsDao[F[_]] {
  def insert(healthCheckDetails: HealthCheckDetails): F[Int]

  def findById(id: ID[HealthCheckDetails]): F[Option[HealthCheckDetails]]

  def findByUserId(id: ID[User]): F[Seq[HealthCheckDetails]]
}