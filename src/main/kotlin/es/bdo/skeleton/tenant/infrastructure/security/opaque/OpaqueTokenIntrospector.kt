package es.bdo.skeleton.tenant.infrastructure.security.opaque

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

interface OpaqueTokenIntrospector {

    fun supports(issuer: String, token: String): Boolean

    fun introspect(token: String): OAuth2AuthenticatedPrincipal
}
