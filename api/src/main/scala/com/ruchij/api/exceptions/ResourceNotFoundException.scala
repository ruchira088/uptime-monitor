package com.ruchij.api.exceptions

case class ResourceNotFoundException(errorMessage: String) extends Exception(errorMessage)
