package com.ruchij.api.services.health.models

case class HealthCheck(database: HealthStatus, internetConnectivity: HealthStatus)
