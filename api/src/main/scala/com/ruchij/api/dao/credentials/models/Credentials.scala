package com.ruchij.api.dao.credentials.models

import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword
import org.joda.time.DateTime
import com.ruchij.api.dao.models.IDs.ID
import com.ruchij.api.dao.user.models.User

case class Credentials(userId: ID[User], createdAt: DateTime, hashedPassword: HashedPassword)