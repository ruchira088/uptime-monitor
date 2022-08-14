package com.ruchij.api.dao.user.models

import com.ruchij.api.dao.user.models.Emails.Email
import org.joda.time.DateTime

case class User(id: String, createdAt: DateTime, email: Email, firstName: String, lastName: String)
