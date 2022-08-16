package com.ruchij.api.types

import com.ruchij.api.dao.models.IDs.ID

trait IdGenerator[F[_], A] {
    val generate: F[ID[A]]
}

object IdGenerator {
    def apply[F[_], A](using idGenerator: IdGenerator[F, A]): IdGenerator[F, A] = idGenerator
}
