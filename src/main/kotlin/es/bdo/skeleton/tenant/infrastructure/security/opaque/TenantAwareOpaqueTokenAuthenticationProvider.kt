package es.bdo.skeleton.tenant.infrastructure.security.opaque

import es.bdo.skeleton.tenant.infrastructure.security.UserInfo
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector

class TenantAwareOpaqueTokenAuthenticationProvider(
    introspector: OpaqueTokenIntrospector
) : AuthenticationProvider {

    private val delegate = OpaqueTokenAuthenticationProvider(introspector)

    override fun authenticate(authentication: Authentication): Authentication {
        val authResult = delegate.authenticate(authentication)
            ?: throw IllegalStateException("Authentication failed")

        val oauth2Principal = authResult.principal as? OAuth2AuthenticatedPrincipal
            ?: throw IllegalStateException("Unexpected principal type")

        val attributes = oauth2Principal.attributes
        val userInfo = UserInfo(
            attributes["sub"] as? String ?: "",
            attributes["preferred_username"] as? String ?: "",
            attributes["email"] as? String ?: "",
            attributes["iss"] as? String ?: "",
            attributes
        )

        return TenantAwareOpaqueAuthenticationToken(
            authentication as BearerTokenAuthenticationToken,
            oauth2Principal,
            authResult.authorities + SimpleGrantedAuthority("ROLE_USER"),
            userInfo
        )
    }

    override fun supports(authentication: Class<*>): Boolean {
        return BearerTokenAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
