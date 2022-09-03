package com.ruchij.api.config

import cats.implicits.toShow
import com.comcast.ip4s.{Host, Port}
import com.ruchij.api.dao.user.models.Passwords.Password

final case class RedisConfiguration(host: Host, port: Port, password: Option[Password]) {
  val url = s"redis://${password.map(_.toString + "@").getOrElse("")}${host.show}:${port.show}"
}