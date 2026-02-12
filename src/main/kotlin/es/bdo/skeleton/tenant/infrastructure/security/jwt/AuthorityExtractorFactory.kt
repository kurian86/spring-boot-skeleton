package es.bdo.skeleton.tenant.infrastructure.security.jwt

class AuthorityExtractorFactory(
    private val extractors: List<AuthorityExtractor>
) {

    constructor() : this(defaultExtractors())

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
