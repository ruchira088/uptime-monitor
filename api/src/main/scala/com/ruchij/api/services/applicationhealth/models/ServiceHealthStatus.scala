package com.ruchij.api.services.applicationhealth.models

case class ServiceHealthStatus(database: HealthStatus, internetConnectivity: HealthStatus) {
  val isHealthy: Boolean = 
    HealthStatus.Healthy == database * internetConnectivity
}
