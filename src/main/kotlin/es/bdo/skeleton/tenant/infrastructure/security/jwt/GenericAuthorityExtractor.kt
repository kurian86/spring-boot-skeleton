package es.bdo.skeleton.tenant.infrastructure.security.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class GenericAuthorityExtractor : AuthorityExtractor {

    companion object {
        private const val ROLE_PREFIX = "ROLE_"
        private const val DEFAULT_ROLE = "USER"
        private val ROLE_CLAIMS = listOf("roles", "authorities", "groups")
    }

    override fun supports(issuer: String): Boolean {
        return true
    }

    override fun extract(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        extractRolesFromClaims(jwt, authorities)
        addDefaultRoleIfEmpty(authorities)

        return authorities
    }

    private fun extractRolesFromClaims(jwt: Jwt, authorities: MutableSet<GrantedAuthority>) {
        val roles = ROLE_CLAIMS.firstNotNullOfOrNull {
            jwt.getClaimAsStringList(it)
        } ?: return

        roles.forEach { role ->
            val authority = if (role.startsWith(ROLE_PREFIX)) role else "$ROLE_PREFIX$role"
            authorities.add(SimpleGrantedAuthority(authority))
        }
    }

    private fun addDefaultRoleIfEmpty(authorities: MutableSet<GrantedAuthority>) {
        if (authorities.isEmpty()) {
            authorities.addRole(DEFAULT_ROLE)
        }
    }
}
