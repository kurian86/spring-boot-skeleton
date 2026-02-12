package es.bdo.skeleton.tenant.infrastructure.security.opaque

import es.bdo.skeleton.tenant.domain.ProviderType
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

interface OpaqueTokenIntrospector {
    fun getType(): ProviderType

    fun supports(token: String): Boolean

    fun introspect(token: String): OAuth2AuthenticatedPrincipal
}
