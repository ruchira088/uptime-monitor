package com.ruchij.api.dao.doobie

import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.types.IdGenerator.IdPrefix
import doobie.enumerated.JdbcType.Date
import doobie.implicits.javasql.*
import doobie.util.Get
import doobie.util.Put
import org.joda.time.DateTime
import org.tpolecat.typename.TypeName

import java.sql.Timestamp
import scala.reflect.ClassTag

object DoobieCodecs {
    given Put[Email] = Put[String].tcontramap[Email](_.toString)

    given Get[Email] = Get[String].temap(stringValue => Email(stringValue).left.map(_.message))

    given Put[DateTime] = Put[Timestamp].tcontramap[DateTime](dateTime => Timestamp(dateTime.getMillis))

    given Get[DateTime] = Get[Timestamp].tmap(timestamp => DateTime(timestamp.getTime))

    given Put[HashedPassword] = Put[String].tcontramap[HashedPassword](_.toString)

    given Get[HashedPassword] = Get[String].map(stringValue => HashedPassword(stringValue))

    given [A]: Put[ID[A]] = Put[String].tcontramap[ID[A]](_.toString)

    given [A: IdPrefix: ClassTag]: Get[ID[A]] = Get[String].temap(id => ID.parse(id).left.map(_.message))
}
