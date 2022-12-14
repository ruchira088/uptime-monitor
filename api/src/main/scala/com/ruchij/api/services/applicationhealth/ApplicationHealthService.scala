package com.ruchij.api.services.applicationhealth

import com.ruchij.api.services.applicationhealth.models.{ServiceHealthStatus, ServiceInformation}
import org.http4s.Uri
import org.http4s.implicits.uri

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

trait ApplicationHealthService[F[_]] {
  def serviceInformation: F[ServiceInformation]

  def healthCheck: F[ServiceHealthStatus]
}

object ApplicationHealthService {
  val ConnectivityUrl: Uri = uri"https://ip.ruchij.com"
  val Timeout: FiniteDuration = 15 seconds
}
