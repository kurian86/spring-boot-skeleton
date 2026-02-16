package es.bdo.skeleton.tenant.application.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken

class TenantOpaqueAuthenticationToken(
    token: BearerTokenAuthenticationToken,
    private val oauth2Principal: OAuth2AuthenticatedPrincipal,
    private val tokenAuthorities: Collection<GrantedAuthority>,
    val userInfo: UserInfo
) : BearerTokenAuthenticationToken(token.token) {

    init {
        super.isAuthenticated = true
        super.eraseCredentials()
    }

    override fun getAuthorities(): Collection<GrantedAuthority> = tokenAuthorities.toList()

    override fun getPrincipal(): UserInfo = userInfo

    override fun getCredentials(): String? = null

    override fun getDetails(): Any? = oauth2Principal.attributes
}
