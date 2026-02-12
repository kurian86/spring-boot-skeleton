package es.bdo.skeleton.tenant.infrastructure.security.opaque

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector as SpringOpaqueTokenIntrospector

class TenantOpaqueTokenIntrospector(
    private val introspectorResolver: OpaqueTokenIntrospectorResolver
) : SpringOpaqueTokenIntrospector {

    override fun introspect(token: String): OAuth2AuthenticatedPrincipal {
        return introspectorResolver.introspect(token)
    }
}
