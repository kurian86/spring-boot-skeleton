package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.infrastructure.security.UserInfo
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class TenantAwareAuthenticationToken(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>,
    val userInfo: UserInfo
) : JwtAuthenticationToken(jwt, authorities) {

    init {
        super.setAuthenticated(true)
    }

    override fun getPrincipal(): UserInfo = userInfo

    override fun toString(): String {
        return "TenantAwareAuthenticationToken(userInfo=$userInfo, authorities=${authorities})"
    }
}
