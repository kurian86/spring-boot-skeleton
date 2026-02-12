package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.infrastructure.security.UserInfo
import org.springframework.security.oauth2.jwt.Jwt

class GenericUserInfoExtractor : UserInfoExtractor {
    override fun supports(issuer: String): Boolean = true

    override fun extract(jwt: Jwt): UserInfo {
        val subject = jwt.subject ?: jwt.getClaimAsString("sub") ?: ""
        val username = extractUsername(jwt)
        val email = jwt.getClaimAsString("email") ?: ""
        val issuer = jwt.issuer?.toString() ?: ""

        return UserInfo(
            subject = subject,
            username = username,
            email = email,
            issuer = issuer,
            attributes = jwt.claims
        )
    }

    private fun extractUsername(jwt: Jwt): String {
        // Try common claim names for username
        return jwt.getClaimAsString("preferred_username")
            ?: jwt.getClaimAsString("username")
            ?: jwt.getClaimAsString("login")
            ?: jwt.getClaimAsString("name")
            ?: jwt.subject
            ?: ""
    }
}
