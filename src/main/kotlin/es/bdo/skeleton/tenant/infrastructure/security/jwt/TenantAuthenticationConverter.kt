package es.bdo.skeleton.tenant.infrastructure.security.jwt

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class TenantAuthenticationConverter(
    private val userInfoExtractorService: UserInfoExtractorService,
    private val grantedAuthoritiesConverter: JwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter(),
) : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val userInfo = extractUserInfo(jwt)
        val authorities = extractAuthorities(jwt)

        return TenantAwareAuthenticationToken(jwt, authorities, userInfo)
    }

    private fun extractUserInfo(jwt: Jwt) = userInfoExtractorService.extractUserInfo(jwt)

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        authorities.addAll(grantedAuthoritiesConverter.convert(jwt) ?: emptySet())
        authorities.add(SimpleGrantedAuthority("ROLE_USER"))

        return authorities
    }
}
