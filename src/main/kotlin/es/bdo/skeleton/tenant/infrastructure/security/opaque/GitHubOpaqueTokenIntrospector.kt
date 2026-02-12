package es.bdo.skeleton.tenant.infrastructure.security.opaque

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

class GitHubOpaqueTokenIntrospector : OpaqueTokenIntrospector {

    companion object {
        private const val API_BASE = "https://api.github.com"
        private val ISSUERS = setOf("github", "github.com")
    }

    private val restClient = RestClient.builder()
        .baseUrl(API_BASE)
        .build()

    override fun supports(issuer: String): Boolean {
        return ISSUERS.any { issuer.contains(it, ignoreCase = true) }
    }

    override fun introspect(token: String): OAuth2AuthenticatedPrincipal {
        try {
            val user = fetchUser(token)
            val orgs = fetchUserOrganizations(token)
            val authorities = buildAuthorities(user, orgs)
            
            val attributes = mapOf<String, Any>(
                "sub" to user.login,
                "login" to user.login,
                "id" to user.id,
                "email" to (user.email ?: ""),
                "orgs" to orgs,
                "iss" to "https://github.com"
            )

            return OAuth2IntrospectionAuthenticatedPrincipal(
                user.login,
                attributes,
                authorities
            )
        } catch (ex: RestClientException) {
            throw BadOpaqueTokenException("Token introspection failed: ${ex.message}")
        }
    }

    private fun fetchUser(token: String): GitHubUserResponse {
        val response = restClient.get()
            .uri("/user")
            .header("Authorization", "Bearer $token")
            .retrieve()
            .toEntity(GitHubUserResponse::class.java)

        if (!response.statusCode.is2xxSuccessful || response.body == null) {
            throw BadOpaqueTokenException("Invalid token or user not found")
        }

        return response.body!!
    }

    private fun fetchUserOrganizations(token: String): List<String> {
        return try {
            restClient.get()
                .uri("/user/orgs")
                .header("Authorization", "Bearer $token")
                .retrieve()
                .toEntity(Array<GitHubOrgResponse>::class.java)
                .body
                ?.map { it.login }
                ?: emptyList()
        } catch (ex: RestClientException) {
            emptyList()
        }
    }

    private fun buildAuthorities(user: GitHubUserResponse, orgs: List<String>): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()
        
        authorities.add(SimpleGrantedAuthority("ROLE_USER"))
        
        if (user.siteAdmin == true) {
            authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
        }
        
        authorities.add(SimpleGrantedAuthority("USER_${user.login}"))
        
        orgs.forEach { org ->
            authorities.add(SimpleGrantedAuthority("ORG_$org"))
        }
        
        return authorities
    }

    data class GitHubUserResponse(
        val login: String,
        val id: Long,
        val email: String? = null,
        @JsonProperty("site_admin")
        val siteAdmin: Boolean? = null
    )

    data class GitHubOrgResponse(
        val login: String
    )
}
