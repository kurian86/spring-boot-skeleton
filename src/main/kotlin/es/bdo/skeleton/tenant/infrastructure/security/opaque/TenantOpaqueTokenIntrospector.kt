package es.bdo.skeleton.tenant.infrastructure.security.opaque

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

interface TenantOpaqueTokenIntrospector {
    fun supports(issuer: String): Boolean

    fun introspect(token: String): OAuth2AuthenticatedPrincipal
}
