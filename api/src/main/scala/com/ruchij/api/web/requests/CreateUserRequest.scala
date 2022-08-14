package com.ruchij.api.web.requests

import com.ruchij.api.dao.user.models.Emails.Email
import com.ruchij.api.dao.user.models.Passwords.Password

case class CreateUserRequest(email: Email, password: Password, firstName: String, lastName: String)