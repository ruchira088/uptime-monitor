package com.ruchij.api.dao.models

import com.ruchij.api.exceptions.ValidationException
import com.ruchij.api.types.IdGenerator.IdPrefix

import scala.reflect.ClassTag

object IDs {
  opaque type ID[A] = String

  object ID {
    def apply[A](id: String): ID[A] = id

    def parse[A](input: String)(using idPrefix: IdPrefix[A], classTag: ClassTag[A]): Either[ValidationException, ID[A]] =
      if input.startsWith(idPrefix.value) 
        then Right(ID[A](input)) 
        else Left(ValidationException(s"$input cannot be an ID of ${classTag.runtimeClass.getCanonicalName}"))
  }
}
