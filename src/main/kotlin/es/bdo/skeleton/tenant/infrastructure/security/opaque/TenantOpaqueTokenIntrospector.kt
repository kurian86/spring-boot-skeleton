package es.bdo.skeleton.tenant.infrastructure.security.opaque

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.domain.OAuthProviderRepository
import es.bdo.skeleton.user.application.UserProvider
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector as SpringOpaqueTokenIntrospector

class TenantOpaqueTokenIntrospector(
    private val introspectorResolver: OpaqueTokenIntrospectorResolver,
    private val oauthProviderRepository: OAuthProviderRepository,
    private val userProvider: UserProvider
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
                val principal = introspectorResolver.introspect(provider.issuer, token)
                return enhancePrincipalWithAuthorities(principal)
            } catch (_: Exception) {
                // Try next provider
            }
        }

        throw BadOpaqueTokenException("Token format not recognized by any provider configured for tenant: $tenantId")
    }

    private fun enhancePrincipalWithAuthorities(principal: OAuth2AuthenticatedPrincipal): OAuth2AuthenticatedPrincipal {
        val email = principal.getAttribute<String>("email")
            ?: principal.getAttribute<String>("sub")
            ?: throw BadOpaqueTokenException("Token does not contain email or subject claim")

        val userAuthorities = userProvider.findUserAuthoritiesByEmail(email)
            .map { SimpleGrantedAuthority(it) }

        return CustomOAuth2AuthenticatedPrincipal(
            principalName = principal.name,
            principalAttributes = principal.attributes,
            principalAuthorities = principal.authorities + userAuthorities
        )
    }
}
