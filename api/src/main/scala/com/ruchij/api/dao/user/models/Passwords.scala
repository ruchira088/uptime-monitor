package com.ruchij.api.dao.user.models

object Passwords {
  opaque type Password = String

  object Password {
    def apply(password: String): Password = password
  }

  extension (password: Password) {
    def print: String = "***************"
  }
}
