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
        key = "#token.hashCode() + '-' + #issuer.hashCode()",
        unless = "#result == null"
    )
    fun introspect(token: String, issuer: String): OAuth2AuthenticatedPrincipal {
        val introspector = introspectors.find { it.supports(issuer) }
            ?: throw IllegalArgumentException("No introspector found for issuer: $issuer")
        return introspector.introspect(token)
    }
}
