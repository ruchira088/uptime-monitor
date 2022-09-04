package com.ruchij.api.services.applicationhealth.models

final case class ServiceHealthStatus(database: HealthStatus, keyValueStore: HealthStatus, internetConnectivity: HealthStatus) {
  val isHealthy: Boolean = 
    HealthStatus.Healthy == database * keyValueStore * internetConnectivity
}
