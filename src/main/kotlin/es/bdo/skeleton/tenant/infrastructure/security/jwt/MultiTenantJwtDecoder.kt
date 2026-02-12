package es.bdo.skeleton.tenant.infrastructure.security.jwt

import com.nimbusds.jwt.JWTParser
import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.domain.OAuthProvider
import es.bdo.skeleton.tenant.domain.OAuthProviderRepository
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class MultiTenantJwtDecoder(
    private val oauthProviderRepository: OAuthProviderRepository,
    private val restOperations: RestOperations = createDefaultRestOperations()
) : JwtDecoder {

    companion object {
        private fun createDefaultRestOperations(): RestOperations {
            val factory = SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(Duration.ofSeconds(10))
                setReadTimeout(Duration.ofSeconds(10))
            }
            return RestTemplate(factory)
        }
    }

    private val jwtDecoderCache = ConcurrentHashMap<String, JwtDecoder>()

    override fun decode(token: String): Jwt {
        val currentTenantId = TenantContext.tenantId
            ?: throw IllegalStateException("No tenant context available")

        val unverifiedJwt = decodeJwtWithoutVerification(token)
        val issuer = unverifiedJwt.issuer?.toString()
            ?: throw IllegalArgumentException("JWT does not contain issuer claim")

        val provider = findProviderForIssuer(currentTenantId, issuer)
            ?: throw IllegalStateException(
                "No OAuth provider found for tenant '$currentTenantId' and issuer '$issuer'"
            )

        val cacheKey = buildCacheKey(currentTenantId, provider)
        val decoder = jwtDecoderCache.computeIfAbsent(cacheKey) {
            createDecoderForProvider(provider)
        }

        return decoder.decode(token)
    }

    private fun decodeJwtWithoutVerification(token: String): Jwt {
        val parsedJwt = JWTParser.parse(token)
        val claims = parsedJwt.jwtClaimsSet

        return Jwt(
            token,
            claims.issueTime?.toInstant(),
            claims.expirationTime?.toInstant(),
            parsedJwt.header.toJSONObject(),
            claims.claims
        )
    }

    private fun findProviderForIssuer(tenantId: String, issuer: String): OAuthProvider? {
        return oauthProviderRepository.findActiveByTenantId(tenantId)
            .find { provider ->
                issuer.startsWith(provider.issuer) || provider.issuer == issuer
            }
    }

    private fun buildCacheKey(tenantId: String, provider: OAuthProvider): String {
        return "$tenantId:${provider.providerType}"
    }

    private fun createDecoderForProvider(provider: OAuthProvider): JwtDecoder {
        validateProviderConfiguration(provider)

        return NimbusJwtDecoder.withJwkSetUri(provider.jwkSetUri)
            .restOperations(restOperations)
            .build()
            .apply {
                setJwtValidator(JwtValidators.createDefaultWithIssuer(provider.issuer))
            }
    }

    private fun validateProviderConfiguration(provider: OAuthProvider) {
        if (!provider.isConfigured()) {
            throw IllegalStateException(
                "OAuth provider '${provider.providerName}' for tenant '${provider.tenantId}' is not properly configured"
            )
        }
    }
}
