package es.bdo.skeleton.tenant.infrastructure.security.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

interface AuthorityExtractor {
    fun supports(issuer: String): Boolean

    fun extract(jwt: Jwt): Collection<GrantedAuthority>

    fun MutableCollection<GrantedAuthority>.addRole(role: String) {
        this.add(SimpleGrantedAuthority("ROLE_$role"))
    }

    fun MutableCollection<GrantedAuthority>.addAuthority(authority: String) {
        this.add(SimpleGrantedAuthority(authority))
    }

    fun MutableCollection<GrantedAuthority>.addAuthority(prefix: String, value: String) {
        this.add(SimpleGrantedAuthority("${prefix}_${value}"))
    }

    fun MutableCollection<GrantedAuthority>.addUserClaim(jwt: Jwt, claimName: String) {
        jwt.getClaimAsString(claimName)?.takeIf { it.isNotBlank() }
            ?.let { this.addAuthority("USER", it) }
    }

    fun MutableCollection<GrantedAuthority>.addListClaims(
        jwt: Jwt,
        claimName: String,
        prefix: String
    ) {
        jwt.getClaimAsStringList(claimName)?.forEach {
            this.addAuthority(prefix, it)
        }
    }
}
