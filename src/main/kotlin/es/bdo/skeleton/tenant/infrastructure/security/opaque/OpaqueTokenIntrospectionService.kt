package es.bdo.skeleton.tenant.infrastructure.security.opaque

import org.springframework.cache.annotation.Cacheable
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Service

@Service
class OpaqueTokenIntrospectionService(
    private val introspectorFactory: OpaqueTokenIntrospectorFactory
) {

    @Cacheable(
        value = ["opaqueTokens"],
        key = "#token.hashCode() + '-' + #issuer.hashCode()",
        unless = "#result == null"
    )
    fun introspect(token: String, issuer: String): OAuth2AuthenticatedPrincipal {
        return introspectorFactory.introspect(token, issuer)
    }
}
