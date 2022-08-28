package com.ruchij.api.types

import com.ruchij.api.dao.models.IDs.ID

import cats.effect.kernel.Sync
import cats.implicits.*
import java.util.UUID

import scala.reflect.ClassTag
import com.ruchij.api.types.IdGenerator.IdPrefix

trait IdGenerator[F[_]] {
    def generate[A: IdPrefix]: F[ID[A]]
}

object IdGenerator {
    trait IdPrefix[A] {
        val value: String
    }

    object IdPrefix {
        def apply[A](using idPrefix: IdPrefix[A]): IdPrefix[A] = idPrefix

        given [A](using classTag: ClassTag[A]): IdPrefix[A] with {
            override val value: String = classTag.runtimeClass.getSimpleName().toLowerCase()
        }
    }

    def apply[F[_]](using idGenerator: IdGenerator[F]): IdGenerator[F] = idGenerator

    given [F[_]: Sync]: IdGenerator[F] with {
        override def generate[A: IdPrefix]: F[ID[A]] = 
            Sync[F].delay(UUID.randomUUID().toString())
                .map { uuid => ID.create[A](s"${IdPrefix[A].value}-$uuid") }
    }
}
