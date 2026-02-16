package es.bdo.skeleton.user.application.exception

import org.springframework.security.core.AuthenticationException

class UserDisabledException(
    message: String = "User account is disabled"
) : AuthenticationException(message)
