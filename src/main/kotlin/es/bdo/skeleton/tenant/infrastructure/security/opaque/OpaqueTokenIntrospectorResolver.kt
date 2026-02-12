package es.bdo.skeleton.tenant.infrastructure.security.opaque

import org.springframework.cache.annotation.Cacheable
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Component

@Component
class OpaqueTokenIntrospectorResolver(
    private val introspectors: List<OpaqueTokenIntrospector> = listOf(GitHubOpaqueTokenIntrospector())
) {

    @Cacheable(
        value = ["opaqueTokens"],
        key = "#issuer.hashCode() + '-' + #token.hashCode()",
        unless = "#result == null"
    )
    fun introspect(issuer: String, token: String): OAuth2AuthenticatedPrincipal {
        val introspector = introspectors.find { it.supports(issuer, token) }
            ?: throw IllegalArgumentException("No introspector found for token. Token does not match any known format.")
        return introspector.introspect(token)
    }
}
