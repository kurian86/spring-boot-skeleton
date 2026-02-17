package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.application.security.UserInfo
import es.bdo.skeleton.user.application.UserProvider
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class TenantJwtAuthenticationConverter(
    private val userProvider: UserProvider,
    private val grantedAuthoritiesConverter: JwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter(),
) : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val userInfo = extractUserInfo(jwt)
        val authorities = extractAuthorities(jwt)

        return TenantJwtAuthenticationToken(jwt, authorities, userInfo)
    }

    private fun extractUserInfo(jwt: Jwt): UserInfo {
        return UserInfo.fromAttributes(jwt.claims)
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        // Add standard JWT authorities
        authorities.addAll(grantedAuthoritiesConverter.convert(jwt) ?: emptySet())

        // Add roles from JWT claim
        val roles = jwt.getClaimAsStringList("roles")
        roles?.forEach { role ->
            val authority = if (role.startsWith("ROLE_")) role else "ROLE_$role"
            authorities.add(SimpleGrantedAuthority(authority))
        }

        return authorities
    }
}
