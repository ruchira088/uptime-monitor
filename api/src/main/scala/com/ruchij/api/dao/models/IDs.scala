package com.ruchij.api.dao.models

object IDs {
  opaque type ID[A] = String

  object ID {
    def apply[A](id: String): ID[A] = id
  }
}
