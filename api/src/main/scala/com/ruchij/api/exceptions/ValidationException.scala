package com.ruchij.api.exceptions

case class ValidationException(message: String) extends Exception(message)
