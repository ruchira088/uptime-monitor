package com.ruchij.migration.models

import com.ruchij.migration.Application

final case class MigrationResult(application: Application, initialVersion: String, targetVersion: String, migrationCount: Int)
