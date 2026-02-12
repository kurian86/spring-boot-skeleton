package es.bdo.skeleton.tenant.infrastructure.security.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class GoogleAuthorityExtractor : AuthorityExtractor {

    companion object {
        private const val CLAIM_EMAIL = "email"
        private const val CLAIM_EMAIL_VERIFIED = "email_verified"
        private const val CLAIM_DOMAIN = "hd"
        private const val ROLE_USER = "USER"
        private val ISSUERS = setOf("google")
    }

    override fun supports(issuer: String): Boolean {
        return ISSUERS.any { issuer.contains(it, ignoreCase = true) }
    }

    override fun extract(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        val email = jwt.getClaimAsString(CLAIM_EMAIL)
        if (email.isNullOrBlank()) return authorities

        authorities.addRole(ROLE_USER)
        extractEmailVerification(jwt, authorities)
        extractDomainAuthority(jwt, authorities)

        return authorities
    }

    private fun extractEmailVerification(jwt: Jwt, authorities: MutableSet<GrantedAuthority>) {
        if (jwt.getClaimAsBoolean(CLAIM_EMAIL_VERIFIED) == true) {
            authorities.addAuthority("EMAIL_VERIFIED")
        }
    }

    private fun extractDomainAuthority(jwt: Jwt, authorities: MutableSet<GrantedAuthority>) {
        jwt.getClaimAsString(CLAIM_DOMAIN)?.takeIf { it.isNotBlank() }
            ?.let { authorities.addAuthority("DOMAIN", it) }
    }
}
