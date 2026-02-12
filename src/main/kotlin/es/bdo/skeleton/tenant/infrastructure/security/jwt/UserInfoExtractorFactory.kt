package es.bdo.skeleton.tenant.infrastructure.security.jwt

import org.springframework.stereotype.Component

@Component
class UserInfoExtractorFactory {

    private val extractors: List<UserInfoExtractor> = listOf(
        GenericUserInfoExtractor()
    )

    fun getExtractor(issuer: String): UserInfoExtractor {
        return extractors.find { it.supports(issuer) }
            ?: GenericUserInfoExtractor()
    }
}
