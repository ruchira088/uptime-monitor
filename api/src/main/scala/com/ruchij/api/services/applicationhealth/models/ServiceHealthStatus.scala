package com.ruchij.api.services.applicationhealth.models

case class ServiceHealthStatus(database: HealthStatus, internetConnectivity: HealthStatus)
