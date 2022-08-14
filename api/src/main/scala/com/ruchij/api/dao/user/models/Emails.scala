package com.ruchij.api.dao.user.models

import com.ruchij.api.exceptions.ValidationException

object Emails {
  opaque type Email = String

  object Email {
    def apply(email: String): Either[ValidationException, Email] = Right(email)
  }

  extension (email: Email) {
    def toString: String = email
  }
}
