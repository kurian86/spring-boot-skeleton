package es.bdo.skeleton.tenant.infrastructure.security.opaque

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Component

@Component
class OpaqueTokenIntrospectorFactory(
    private val introspectors: List<TenantOpaqueTokenIntrospector> = defaultIntrospectors()
) {
    companion object {
        fun defaultIntrospectors(): List<TenantOpaqueTokenIntrospector> {
            return listOf(
                GitHubOpaqueTokenIntrospector()
            )
        }
    }

    fun introspect(token: String, issuer: String): OAuth2AuthenticatedPrincipal {
        val introspector = getIntrospector(issuer)
        return introspector.introspect(token)
    }

    private fun getIntrospector(issuer: String): TenantOpaqueTokenIntrospector {
        return introspectors.find { it.supports(issuer) }
            ?: throw IllegalArgumentException("No introspector found for issuer: $issuer")
    }
}
