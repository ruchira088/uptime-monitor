package com.ruchij.api.dao.models

import com.ruchij.api.exceptions.ValidationException
import com.ruchij.api.types.IdGenerator.IdPrefix

import scala.reflect.ClassTag

object IDs {
  opaque type ID[A] = String

  object ID {
    def create[A: IdPrefix](id: String): ID[A] = IdPrefix[A].value + id

    def parse[A](input: String)(using idPrefix: IdPrefix[A], classTag: ClassTag[A]): Either[ValidationException, ID[A]] =
      if input.startsWith(idPrefix.value) 
        then Right(ID.create[A](input.substring(idPrefix.value.length)))
        else Left(ValidationException(s"$input cannot be an ID of ${classTag.runtimeClass.getCanonicalName}"))
  }
}
