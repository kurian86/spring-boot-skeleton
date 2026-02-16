package es.bdo.skeleton.tenant.application.security

import es.bdo.skeleton.tenant.application.exception.InvalidTokenException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class TokenValidator {

    fun validateAndExtractUserInfo(authentication: Authentication): UserInfo {
        return when (authentication) {
            is TenantOpaqueAuthenticationToken -> authentication.principal
            is TenantJwtAuthenticationToken -> authentication.principal
            else -> throw InvalidTokenException("Unsupported authentication type: ${authentication.javaClass.simpleName}")
        }
    }
}
