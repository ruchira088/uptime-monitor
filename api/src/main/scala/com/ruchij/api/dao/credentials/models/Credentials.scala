package com.ruchij.api.dao.credentials.models

import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword
import org.joda.time.DateTime

case class Credentials(userId: String, createdAt: DateTime, hashedPassword: HashedPassword)