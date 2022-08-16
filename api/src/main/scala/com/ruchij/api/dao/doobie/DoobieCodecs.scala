package com.ruchij.api.dao.doobie

import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.Emails.Email
import doobie.implicits.javasql.*
import doobie.util.Put
import doobie.util.Get
import org.joda.time.DateTime

import java.sql.Timestamp
import doobie.enumerated.JdbcType.Date
import org.tpolecat.typename.TypeName

object DoobieCodecs {
    given Put[Email] = Put[String].tcontramap[Email](_.toString)

    given Get[Email] = Get[String].temap(stringValue => Email(stringValue).left.map(_.message))

    given Put[DateTime] = Put[Timestamp].tcontramap[DateTime](dateTime => Timestamp(dateTime.getMillis()))

    given Get[DateTime] = Get[Timestamp].tmap(timestamp => DateTime(timestamp.getTime()))

    given Put[HashedPassword] = Put[String].tcontramap[HashedPassword](_.toString)

    given Get[HashedPassword] = Get[String].map(stringValue => HashedPassword(stringValue))

    given [A]: Put[ID[A]] = Put[String].tcontramap[ID[A]](_.toString)

    given [A]: Get[ID[A]] = Get[String].map(ID.apply[A])
}
