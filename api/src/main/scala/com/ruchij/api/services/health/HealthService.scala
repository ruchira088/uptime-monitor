package com.ruchij.api.services.health

import com.ruchij.api.services.health.models.{HealthCheck, ServiceInformation}
import org.http4s.Uri
import org.http4s.implicits.uri

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

trait HealthService[F[_]] {
  def serviceInformation: F[ServiceInformation]

  def healthCheck: F[HealthCheck]
}

object HealthService {
  val ConnectivityUrl: Uri = uri"https://ip.ruchij.com"
  val Timeout: FiniteDuration = 30 seconds
}
