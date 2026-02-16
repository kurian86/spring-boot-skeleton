package es.bdo.skeleton.tenant.application.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class TenantJwtAuthenticationToken(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>,
    val userInfo: UserInfo
) : JwtAuthenticationToken(jwt, authorities) {

    init {
        super.setAuthenticated(true)
    }

    override fun getPrincipal(): UserInfo = userInfo
}
