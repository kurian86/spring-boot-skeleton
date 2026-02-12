package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.infrastructure.security.UserInfo
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class UserInfoExtractorResolver(
    private val extractors: List<UserInfoExtractor> = listOf(GenericUserInfoExtractor())
) {

    fun extractUserInfo(jwt: Jwt): UserInfo {
        val issuer = jwt.issuer?.toString() ?: ""
        val extractor = extractors.find { it.supports(issuer) }
            ?: GenericUserInfoExtractor()
        return extractor.extract(jwt)
    }
}
