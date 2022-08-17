package com.ruchij.api.types

import com.ruchij.api.dao.models.IDs.ID

import cats.effect.kernel.Sync
import cats.implicits.*
import java.util.UUID

import scala.reflect.ClassTag

trait IdGenerator[F[_], A] {
    val generate: F[ID[A]]
}

object IdGenerator {
    def apply[F[_], A](using idGenerator: IdGenerator[F, A]): IdGenerator[F, A] = idGenerator

    given [F[_]: Sync, A](using classTag: ClassTag[A]): IdGenerator[F, A] with {
        override val generate: F[ID[A]] = 
            Sync[F].delay(UUID.randomUUID().toString())
                .map { uuid => 
                    val prefix = classTag.runtimeClass.getSimpleName().toLowerCase()

                    ID[A](s"$prefix-$uuid")
                }
    }
}
