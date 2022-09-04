package com.ruchij.api.exceptions

final case class ValidationException(message: String) extends Exception(message)
