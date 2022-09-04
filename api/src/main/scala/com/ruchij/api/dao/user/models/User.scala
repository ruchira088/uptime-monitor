package com.ruchij.api.dao.user.models

import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.Emails.Email
import org.joda.time.DateTime

final case class User(id: ID[User], createdAt: DateTime, email: Email, firstName: String, lastName: String)
