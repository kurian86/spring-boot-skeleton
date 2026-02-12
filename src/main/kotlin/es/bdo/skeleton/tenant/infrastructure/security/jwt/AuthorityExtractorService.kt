package es.bdo.skeleton.tenant.infrastructure.security.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class AuthorityExtractorService(
    private val authorityExtractorFactory: AuthorityExtractorFactory
) {

    fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val issuer = jwt.issuer?.toString() ?: ""
        val extractor = authorityExtractorFactory.getExtractor(issuer)
        return extractor.extract(jwt)
    }
}
