package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.infrastructure.security.UserInfo
import org.springframework.security.oauth2.jwt.Jwt

class GenericUserInfoExtractor : UserInfoExtractor {

    override fun supports(issuer: String): Boolean = true

    override fun extract(jwt: Jwt): UserInfo = UserInfo.fromAttributes(jwt.claims)
}
