package es.bdo.skeleton.tenant.infrastructure.security.opaque

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

class OpaqueTokenIntrospectorFactory(
    private val introspectors: List<OpaqueTokenIntrospector>
) {

    constructor() : this(defaultIntrospectors())

    companion object {
        fun defaultIntrospectors(): List<OpaqueTokenIntrospector> {
            return listOf(
                GitHubOpaqueTokenIntrospector()
            )
        }
    }

    fun getIntrospector(issuer: String): OpaqueTokenIntrospector {
        return introspectors.find { it.supports(issuer) }
            ?: throw IllegalArgumentException("No introspector found for issuer: $issuer")
    }

    fun introspect(token: String, issuer: String): OAuth2AuthenticatedPrincipal {
        val introspector = getIntrospector(issuer)
        return introspector.introspect(token)
    }
}
