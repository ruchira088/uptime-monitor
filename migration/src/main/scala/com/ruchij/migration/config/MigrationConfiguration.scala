package com.ruchij.migration.config

import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.derivation.default.*
import pureconfig.ConfigReader
import com.ruchij.migration.Application
import pureconfig.ConfigCursor

case class MigrationConfiguration(databaseConfiguration: DatabaseConfiguration, application: Option[Application]) derives ConfigReader

object MigrationConfiguration {
    given ConfigReader[Application] = 
        ConfigReader.fromNonEmptyStringOpt(input => Application.values.find(_.name.equalsIgnoreCase(input)))

    def load(configObjectSource: ConfigObjectSource): Either[ConfigReaderException[MigrationConfiguration], MigrationConfiguration] =
        configObjectSource.load[MigrationConfiguration].left.map { failures => ConfigReaderException[MigrationConfiguration](failures) }
}
