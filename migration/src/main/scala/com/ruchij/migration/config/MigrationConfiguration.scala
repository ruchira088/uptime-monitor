package com.ruchij.migration.config

import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.derivation.default.*
import pureconfig.ConfigReader
import com.ruchij.migration.Application
import pureconfig.ConfigCursor

final case class MigrationConfiguration(databaseConfiguration: DatabaseConfiguration, application: Option[Application]) derives ConfigReader

object MigrationConfiguration {
    def load(configObjectSource: ConfigObjectSource): Either[ConfigReaderException[MigrationConfiguration], MigrationConfiguration] =
        configObjectSource.load[MigrationConfiguration].left.map { failures => ConfigReaderException[MigrationConfiguration](failures) }
}
