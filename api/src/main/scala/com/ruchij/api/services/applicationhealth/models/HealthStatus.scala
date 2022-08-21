package com.ruchij.api.services.applicationhealth.models

enum HealthStatus extends Enum[HealthStatus] {
  case Healthy, Unhealthy
}

object HealthStatus {
  extension (healthStatus: HealthStatus) {
    def * (other: HealthStatus): HealthStatus =
      if HealthStatus.Healthy == healthStatus then other else HealthStatus.Unhealthy
  }
}
