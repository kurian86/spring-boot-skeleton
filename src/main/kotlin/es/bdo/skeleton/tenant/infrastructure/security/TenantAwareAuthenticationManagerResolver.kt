package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.infrastructure.security.jwt.TenantAuthenticationConverter
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector

class TenantAwareAuthenticationManagerResolver(
    private val jwtDecoder: JwtDecoder,
    private val opaqueTokenIntrospector: OpaqueTokenIntrospector
) : AuthenticationManagerResolver<HttpServletRequest> {

    private val jwtAuthenticationManager: AuthenticationManager by lazy {
        val jwtAuthenticationProvider = JwtAuthenticationProvider(jwtDecoder)
        jwtAuthenticationProvider.setJwtAuthenticationConverter(TenantAuthenticationConverter())
        ProviderManager(jwtAuthenticationProvider)
    }

    private val opaqueAuthenticationManager: AuthenticationManager by lazy {
        ProviderManager(OpaqueTokenAuthenticationProvider(opaqueTokenIntrospector))
    }

    override fun resolve(request: HttpServletRequest): AuthenticationManager {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw IllegalStateException("No Bearer token found in request")
        }

        val token = authHeader.substring(7)

        return if (isJwtToken(token)) {
            jwtAuthenticationManager
        } else {
            opaqueAuthenticationManager
        }
    }

    private fun isJwtToken(token: String): Boolean {
        return try {
            val parts = token.split(".")
            parts.size == 3 && parts.all { it.isNotBlank() }
        } catch (e: Exception) {
            false
        }
    }
}
