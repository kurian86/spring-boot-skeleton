package es.bdo.skeleton.tenant.infrastructure.security.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class AzureAuthorityExtractor : AuthorityExtractor {

    companion object {
        private const val CLAIM_ROLES = "roles"
        private const val CLAIM_GROUPS = "groups"
        private const val CLAIM_APP_ROLES = "app_roles"
        private val ISSUERS = setOf("microsoft", "azure")
    }

    override fun supports(issuer: String): Boolean {
        return ISSUERS.any { issuer.contains(it, ignoreCase = true) }
    }

    override fun extract(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        extractRoles(jwt, authorities)
        extractGroups(jwt, authorities)
        extractAppRoles(jwt, authorities)

        return authorities
    }

    private fun extractRoles(jwt: Jwt, authorities: MutableSet<GrantedAuthority>) {
        authorities.addListClaims(jwt, CLAIM_ROLES, "ROLE")
    }

    private fun extractGroups(jwt: Jwt, authorities: MutableSet<GrantedAuthority>) {
        authorities.addListClaims(jwt, CLAIM_GROUPS, "AZURE_GROUP")
    }

    private fun extractAppRoles(jwt: Jwt, authorities: MutableSet<GrantedAuthority>) {
        authorities.addListClaims(jwt, CLAIM_APP_ROLES, "APP_ROLE")
    }
}
