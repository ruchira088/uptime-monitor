package com.ruchij.api.services.authentication.models

import com.ruchij.api.dao.user.models.User

final case class AuthenticationSession(user: User, authenticationToken: AuthenticationToken)
