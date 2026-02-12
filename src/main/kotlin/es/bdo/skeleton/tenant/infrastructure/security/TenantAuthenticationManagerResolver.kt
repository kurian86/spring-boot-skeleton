package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.infrastructure.security.opaque.TenantOpaqueAuthenticationProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector

class TenantAuthenticationManagerResolver(
    private val jwtDecoder: JwtDecoder,
    private val jwtAuthenticationConverter: Converter<Jwt, AbstractAuthenticationToken>,
    private val opaqueTokenIntrospector: OpaqueTokenIntrospector
) : AuthenticationManagerResolver<HttpServletRequest> {

    private val jwtAuthenticationManager: AuthenticationManager by lazy {
        val provider = JwtAuthenticationProvider(jwtDecoder)
        provider.setJwtAuthenticationConverter(jwtAuthenticationConverter)
        ProviderManager(provider)
    }

    private val opaqueAuthenticationManager: AuthenticationManager by lazy {
        ProviderManager(TenantOpaqueAuthenticationProvider(opaqueTokenIntrospector))
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
        val parts = token.split(".")
        return parts.size == 3 && parts.all { it.isNotBlank() }
    }
}
