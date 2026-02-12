package es.bdo.skeleton.tenant.infrastructure.security.opaque

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.domain.OAuthProviderRepository
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector as SpringOpaqueTokenIntrospector

class TenantOpaqueTokenIntrospector(
    private val introspectorResolver: OpaqueTokenIntrospectorResolver,
    private val oauthProviderRepository: OAuthProviderRepository
) : SpringOpaqueTokenIntrospector {

    override fun introspect(token: String): OAuth2AuthenticatedPrincipal {
        val tenantId = TenantContext.tenantId
            ?: throw IllegalStateException("No tenant context available")

        val opaqueProviders = oauthProviderRepository.findActiveByTenantId(tenantId)
            .filter { it.isOpaque }

        if (opaqueProviders.isEmpty()) {
            throw BadOpaqueTokenException("No opaque token providers configured for tenant: $tenantId")
        }


        for (provider in opaqueProviders) {
            try {
                return introspectorResolver.introspect(provider.issuer, token)
            } catch (_: Exception) {
            }
        }

        throw BadOpaqueTokenException("Token format not recognized by any provider configured for tenant: $tenantId")
    }
}
