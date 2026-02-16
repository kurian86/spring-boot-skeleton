package es.bdo.skeleton.tenant.infrastructure.security.opaque

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

class GitHubOpaqueTokenIntrospector : OpaqueTokenIntrospector {

    companion object {
        private const val API_BASE = "https://api.github.com"
        private const val ISSUER = "https://github.com"
        private val TOKEN_PREFIXES = setOf("ghp_", "gho_", "ghu_", "ghs_", "ghr_")
    }

    private val restClient = RestClient.builder()
        .baseUrl(API_BASE)
        .build()

    override fun supports(issuer: String, token: String): Boolean {
        return ISSUER.equals(issuer, ignoreCase = true) && TOKEN_PREFIXES.any { token.startsWith(it) }
    }

    override fun introspect(token: String): OAuth2AuthenticatedPrincipal {
        try {
            val user = fetchUser(token)

            val attributes = mapOf<String, Any>(
                "sub" to user.login,
                "preferred_username" to user.login,
                "email" to (user.email ?: ""),
                "iss" to ISSUER,
                "id" to user.id
            )

            return OAuth2IntrospectionAuthenticatedPrincipal(
                user.login,
                attributes,
                listOf()
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

    data class GitHubUserResponse(
        val id: Long,
        val login: String,
        val email: String? = null
    )
}
