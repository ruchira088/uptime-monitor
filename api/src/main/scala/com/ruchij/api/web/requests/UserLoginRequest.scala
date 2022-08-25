package com.ruchij.api.web.requests

import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password

final case class UserLoginRequest(email: Email, password: Password)
