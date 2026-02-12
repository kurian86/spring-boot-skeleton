package es.bdo.skeleton.tenant.infrastructure.security.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class TenantAwareAuthenticationToken(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>,
    val tenantId: String
) : JwtAuthenticationToken(jwt, authorities) {

    private val principalName: String = extractPrincipal(jwt)

    init {
        super.setAuthenticated(true)
    }

    private fun extractPrincipal(jwt: Jwt): String {
        return jwt.getClaimAsString("preferred_username")
            ?: jwt.getClaimAsString("login")
            ?: jwt.getClaimAsString("email")
            ?: jwt.getClaimAsString("upn")
            ?: jwt.getClaimAsString("sub")
            ?: "unknown"
    }

    override fun getPrincipal(): String = principalName

    override fun toString(): String {
        return "TenantAwareAuthenticationToken(tenantId=$tenantId, principal=$principalName, authorities=${authorities})"
    }
}
