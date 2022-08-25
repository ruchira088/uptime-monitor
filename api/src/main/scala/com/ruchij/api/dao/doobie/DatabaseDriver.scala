package com.ruchij.api.dao.doobie

import java.sql.Driver
import org.{h2, postgresql}
import scala.reflect.ClassTag

enum DatabaseDriver[A <: Driver](using classTag: ClassTag[A]) extends Enum[DatabaseDriver[_]] { self =>
  case PostgreSQL extends DatabaseDriver[postgresql.Driver]
  case H2 extends DatabaseDriver[h2.Driver]

  val protocol: String = self.name().toLowerCase()

  val driver: String = classTag.runtimeClass.getName()
}

object DatabaseDriver {
  def parse(connectionUrl: String): Either[Throwable, DatabaseDriver[_]] =
    DatabaseDriver.values
      .find { databaseDriver => connectionUrl.startsWith(s"jdbc:${databaseDriver.protocol}") }
      .toRight {
        IllegalArgumentException(s"Unable to extract supported database driver from connection URL. Supported database drivers: ${DatabaseDriver.values.map(_.name().mkString(", "))}")
      }
}