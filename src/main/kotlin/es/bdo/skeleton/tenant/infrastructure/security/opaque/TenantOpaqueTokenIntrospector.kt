package es.bdo.skeleton.tenant.infrastructure.security.opaque

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.domain.OAuthProvider
import es.bdo.skeleton.tenant.domain.OAuthProviderRepository
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector as SpringOpaqueTokenIntrospector

class TenantOpaqueTokenIntrospector(
    private val oauthProviderRepository: OAuthProviderRepository,
    private val introspectorResolver: OpaqueTokenIntrospectorResolver
) : SpringOpaqueTokenIntrospector {

    override fun introspect(token: String): OAuth2AuthenticatedPrincipal {
        val tenantId = TenantContext.tenantId
            ?: throw IllegalStateException("No tenant context available")

        val opaqueProvider = oauthProviderRepository.findActiveByTenantId(tenantId)
            .find { it.providerType == OAuthProvider.ProviderType.GITHUB }
            ?: throw BadOpaqueTokenException(
                "No opaque token provider configured for tenant: $tenantId"
            )

        return introspectorResolver.introspect(token, opaqueProvider.issuer)
    }
}
