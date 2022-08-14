package com.ruchij.api.services.health

import com.ruchij.api.services.health.models.ServiceInformation

trait HealthService[F[_]] {
  def serviceInformation: F[ServiceInformation]
}
