package com.ruchij.migration

import org.flywaydb.core.api.Location
import pureconfig.generic.derivation.EnumConfigReader

enum Application(private val flywayScriptsFolder: String) derives EnumConfigReader {
  case Api extends Application("api")
  case Batch extends Application("batch")
  case Worker extends Application("worker")
  
  val flywayScripts: Location = Location(s"db/migration/$flywayScriptsFolder")
}
