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

class TenantJwtDecoder(
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

    private val decoderCache = ConcurrentHashMap<String, JwtDecoder>()

    override fun decode(token: String): Jwt {
        val tenantId = TenantContext.getOrNull()
            ?: throw IllegalStateException("No tenant context available")

        val unverifiedJwt = parseWithoutVerification(token)
        val issuer = unverifiedJwt.issuer?.toString()
            ?: throw IllegalArgumentException("JWT does not contain issuer claim")

        val provider = findProviderForIssuer(tenantId, issuer)
            ?: throw IllegalStateException(
                "No OAuth provider found for tenant '$tenantId' and issuer '$issuer'"
            )

        val cacheKey = "$tenantId:${provider.issuer}"
        val decoder = decoderCache.computeIfAbsent(cacheKey) {
            createDecoderForProvider(provider)
        }

        return decoder.decode(token)
    }

    private fun parseWithoutVerification(token: String): Jwt {
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

    private fun createDecoderForProvider(provider: OAuthProvider): JwtDecoder {
        require(provider.isConfigured()) {
            "OAuth provider '${provider.name}' for tenant '${provider.tenantId}' is not properly configured"
        }

        return NimbusJwtDecoder.withJwkSetUri(provider.jwkSetUri)
            .restOperations(restOperations)
            .build()
            .apply {
                setJwtValidator(JwtValidators.createDefaultWithIssuer(provider.issuer))
            }
    }
}
