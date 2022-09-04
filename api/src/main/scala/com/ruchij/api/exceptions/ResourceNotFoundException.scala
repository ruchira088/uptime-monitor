package com.ruchij.api.exceptions

final case class ResourceNotFoundException(errorMessage: String) extends Exception(errorMessage)
