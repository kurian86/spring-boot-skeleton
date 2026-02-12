package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.application.TenantContext
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class TenantAuthenticationConverter(
    private val authorityExtractorService: AuthorityExtractorService,
    private val grantedAuthoritiesConverter: JwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter(),
) : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val authorities = extractAuthorities(jwt)
        val tenantId = TenantContext.tenantId
            ?: throw IllegalStateException("No tenant context available")

        return TenantAwareAuthenticationToken(jwt, authorities, tenantId)
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        authorities.addAll(grantedAuthoritiesConverter.convert(jwt) ?: emptySet())
        authorities.addAll(authorityExtractorService.extractAuthorities(jwt))

        return authorities
    }
}
