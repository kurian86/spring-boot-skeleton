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
        val authorities = extractAuthorities(jwt, userInfo.email)

        return TenantJwtAuthenticationToken(jwt, authorities, userInfo)
    }

    private fun extractUserInfo(jwt: Jwt): UserInfo {
        return UserInfo.fromAttributes(jwt.claims)
    }

    private fun extractAuthorities(jwt: Jwt, email: String): Collection<GrantedAuthority> {
        val userAuthorities = userProvider.findUserAuthoritiesByEmail(email)
            .map { SimpleGrantedAuthority(it) }

        val authorities = mutableSetOf<GrantedAuthority>()
        authorities.addAll(grantedAuthoritiesConverter.convert(jwt) ?: emptySet())
        authorities.addAll(userAuthorities)
        return authorities
    }
}
