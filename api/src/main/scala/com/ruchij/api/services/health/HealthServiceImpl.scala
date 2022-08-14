package com.ruchij.api.services.health

import cats.effect.Sync
import cats.implicits.*
import com.ruchij.api.config.BuildInformation
import com.ruchij.api.services.health.models.ServiceInformation
import com.ruchij.api.types.JodaClock

class HealthServiceImpl[F[_]: JodaClock: Sync](buildInformation: BuildInformation) extends HealthService[F] {
  override val serviceInformation: F[ServiceInformation] =
    JodaClock[F].timestamp
      .flatMap(timestamp => ServiceInformation.create(timestamp, buildInformation))
}
