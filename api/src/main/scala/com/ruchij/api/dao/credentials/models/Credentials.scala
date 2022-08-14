package com.ruchij.api.dao.credentials.models

import com.ruchij.api.dao.credentials.models.HashedPasswords.HashedPassword

case class Credentials(userId: String, createdAt: String, hashedPassword: HashedPassword)