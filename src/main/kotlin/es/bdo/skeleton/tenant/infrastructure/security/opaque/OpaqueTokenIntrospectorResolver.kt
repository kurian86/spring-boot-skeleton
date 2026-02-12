package es.bdo.skeleton.tenant.infrastructure.security.opaque

import es.bdo.skeleton.tenant.domain.ProviderType
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Component

@Component
class OpaqueTokenIntrospectorResolver(
    private val introspectors: List<OpaqueTokenIntrospector> = listOf(GitHubOpaqueTokenIntrospector())
) {

    @Cacheable(
        value = ["opaqueTokens"],
        key = "#token.hashCode()",
        unless = "#result == null"
    )
    fun introspect(token: String, type: ProviderType): OAuth2AuthenticatedPrincipal {
        val introspector = introspectors.filter { it.getType() == type }.find { it.supports(token) }
            ?: throw IllegalArgumentException("No introspector found for token. Token does not match any known format.")
        return introspector.introspect(token)
    }
}
