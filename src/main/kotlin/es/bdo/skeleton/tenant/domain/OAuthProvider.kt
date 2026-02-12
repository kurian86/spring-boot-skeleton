package es.bdo.skeleton.tenant.domain

import java.time.ZonedDateTime
import java.util.*

data class OAuthProvider(
    val id: UUID,
    val tenantId: String,
    val type: ProviderType,
    val name: String,
    val clientId: String,
    val clientSecret: String,
    val issuer: String,
    val authorizationUri: String?,
    val tokenUri: String?,
    val userInfoUri: String?,
    val jwkSetUri: String,
    val scope: String?,
    val isOpaque: Boolean,
    val isActive: Boolean,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
) {

    fun getScopesList(): List<String> {
        return scope?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    fun isConfigured(): Boolean {
        return clientId.isNotBlank() && 
               clientSecret.isNotBlank() && 
               issuer.isNotBlank() && 
               jwkSetUri.isNotBlank()
    }
}
