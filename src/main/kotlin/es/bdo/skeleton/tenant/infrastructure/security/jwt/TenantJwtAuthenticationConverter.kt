package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.application.security.UserInfo
import es.bdo.skeleton.user.application.UserRegistrationService
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class TenantJwtAuthenticationConverter(
    private val userRegistrationService: UserRegistrationService
) : Converter<Jwt, AbstractAuthenticationToken> {

    private val grantedAuthoritiesConverter: JwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter().apply {
        setAuthorityPrefix("ROLE_")
        setAuthoritiesClaimName("roles")
    }

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val userInfo = extractUserInfo(jwt)
        val authorities = extractAuthorities(jwt)

        userRegistrationService.ensureUserExists(
            userInfo.email,
            userInfo.attributes["name"] as? String ?: userInfo.username,
            userInfo.subject
        )

        return TenantJwtAuthenticationToken(jwt, authorities, userInfo)
    }

    private fun extractUserInfo(jwt: Jwt): UserInfo {
        return UserInfo.fromAttributes(jwt.claims)
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        return grantedAuthoritiesConverter.convert(jwt).mapNotNull {
            it.authority?.let { SimpleGrantedAuthority(it.uppercase()) }
        }
    }
}
