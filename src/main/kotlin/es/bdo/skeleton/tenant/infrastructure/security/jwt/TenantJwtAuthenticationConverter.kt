package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.application.security.TenantJwtAuthenticationToken
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class TenantJwtAuthenticationConverter(
    private val userInfoExtractorResolver: UserInfoExtractorResolver,
    private val grantedAuthoritiesConverter: JwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter(),
) : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val userInfo = userInfoExtractorResolver.extractUserInfo(jwt)
        val authorities = extractAuthorities(jwt)

        return TenantJwtAuthenticationToken(jwt, authorities, userInfo)
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()
        authorities.addAll(grantedAuthoritiesConverter.convert(jwt) ?: emptySet())
        authorities.add(SimpleGrantedAuthority("ROLE_USER"))
        return authorities
    }
}
