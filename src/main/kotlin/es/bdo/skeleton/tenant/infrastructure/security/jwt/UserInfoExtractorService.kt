package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.infrastructure.security.UserInfo
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class UserInfoExtractorService(
    private val extractorFactory: UserInfoExtractorFactory
) {

    fun extractUserInfo(jwt: Jwt): UserInfo {
        val issuer = jwt.issuer?.toString() ?: ""
        val extractor = extractorFactory.getExtractor(issuer)
        return extractor.extract(jwt)
    }
}
