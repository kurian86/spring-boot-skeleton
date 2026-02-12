package es.bdo.skeleton.tenant.infrastructure.security.jwt

import org.springframework.stereotype.Component

@Component
class AuthorityExtractorFactory(
    private val extractors: List<AuthorityExtractor> = defaultExtractors()
) {
    companion object {
        fun defaultExtractors(): List<AuthorityExtractor> {
            return listOf(
                GoogleAuthorityExtractor(),
                AzureAuthorityExtractor(),
                GenericAuthorityExtractor()
            )
        }
    }

    fun getExtractor(issuer: String): AuthorityExtractor {
        return extractors.find { it.supports(issuer) }
            ?: throw IllegalArgumentException("No extractor found for issuer: $issuer")
    }
}
