package com.ruchij.api.dao.credentials.models

import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User
import org.joda.time.DateTime

final case class Credentials(userId: ID[User], createdAt: DateTime, hashedPassword: HashedPassword)