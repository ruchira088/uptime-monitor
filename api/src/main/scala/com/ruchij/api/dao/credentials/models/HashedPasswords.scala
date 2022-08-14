package com.ruchij.api.dao.credentials.models

object HashedPasswords {
  opaque type HashedPassword = String
  
  object HashedPassword {
    def apply(hashedPassword: String): HashedPassword = hashedPassword
  }
}
