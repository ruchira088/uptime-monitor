package com.ruchij.migration

import org.flywaydb.core.api.Location

enum Application(private val flywayScriptsFolder: String) extends Enum[Application] {
  case Api extends Application("api")
  case Batch extends Application("batch")
  case Worker extends Application("worker")
  
  val flywayScripts: Location = Location(s"db/migration/$flywayScriptsFolder")
}
