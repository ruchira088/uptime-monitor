package com.ruchij.api.services.health.models

case class ServiceHealthStatus(database: HealthStatus, internetConnectivity: HealthStatus)
