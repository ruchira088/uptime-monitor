package com.ruchij.api.config

import cats.ApplicativeError
import com.ruchij.api.config.BuildInformation
import com.ruchij.api.config.ConfigReaders.given
import com.ruchij.api.types.FunctionKTypes.*
import pureconfig.error.ConfigReaderException
import pureconfig.generic.derivation.default.*
import pureconfig.{ConfigObjectSource, ConfigReader}

case class ServiceConfiguration(httpConfiguration: HttpConfiguration, buildInformation: BuildInformation) derives ConfigReader

object ServiceConfiguration {
  def parse[F[_]: [G[_]] =>> ApplicativeError[G, Throwable]](configObjectSource: ConfigObjectSource): F[ServiceConfiguration] =
    configObjectSource.load[ServiceConfiguration].left.map(ConfigReaderException.apply[ServiceConfiguration]).toType[F, Throwable]
}
