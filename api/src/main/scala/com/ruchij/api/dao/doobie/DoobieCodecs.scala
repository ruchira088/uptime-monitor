package com.ruchij.api.dao.doobie

import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword
import com.ruchij.api.dao.user.models.Emails.Email
import doobie.implicits.javasql.*
import doobie.util.Put
import org.joda.time.DateTime

import java.sql.Timestamp

object DoobieCodecs {
    given Put[Email] = Put[String].tcontramap[Email](_.toString)

    given Put[DateTime] = Put[Timestamp].tcontramap[DateTime](dateTime => Timestamp(dateTime.getMillis()))

    given Put[HashedPassword] = Put[String].tcontramap[HashedPassword](_.toString)
}
