package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.infrastructure.security.UserInfo
import org.springframework.security.oauth2.jwt.Jwt

interface UserInfoExtractor {
    fun supports(issuer: String): Boolean
    
    fun extract(jwt: Jwt): UserInfo
}
