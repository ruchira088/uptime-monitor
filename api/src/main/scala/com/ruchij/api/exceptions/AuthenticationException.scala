package com.ruchij.api.exceptions

final case class AuthenticationException(message: String) extends Exception(message)
