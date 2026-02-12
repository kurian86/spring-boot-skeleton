package es.bdo.skeleton.tenant.infrastructure.security.opaque

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.domain.OAuthProvider
import es.bdo.skeleton.tenant.domain.OAuthProviderRepository
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector

class TenantAwareOpaqueTokenIntrospector(
    private val oauthProviderRepository: OAuthProviderRepository,
    private val introspectionService: OpaqueTokenIntrospectionService
) : OpaqueTokenIntrospector {

    override fun introspect(token: String): OAuth2AuthenticatedPrincipal {
        val tenantId = TenantContext.tenantId
            ?: throw IllegalStateException("No tenant context available")

        val providers = oauthProviderRepository.findActiveByTenantId(tenantId)

        val opaqueProvider = providers.find { provider ->
            isOpaqueTokenProvider(provider.providerType)
        }

        if (opaqueProvider == null) {
            throw BadOpaqueTokenException(
                "No opaque token provider configured for tenant: $tenantId"
            )
        }

        return introspectionService.introspect(token, opaqueProvider.issuer)
    }

    private fun isOpaqueTokenProvider(providerType: OAuthProvider.ProviderType): Boolean {
        return providerType == OAuthProvider.ProviderType.GITHUB
    }
}
