package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.application.TenantContext
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component

@Component
class TenantAuthenticationConverter(
    private val grantedAuthoritiesConverter: JwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
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

        val issuer = jwt.issuer?.toString() ?: ""
        val extractor = AuthorityExtractorFactory().getExtractor(issuer)
        authorities.addAll(extractor.extract(jwt))

        return authorities
    }
}
