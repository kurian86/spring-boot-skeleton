package es.bdo.skeleton.tenant.domain

import java.time.ZonedDateTime
import java.util.*

data class OAuthProvider(
    val id: UUID,
    val tenantId: String,
    val name: String,
    val issuer: String,
    val jwkSetUri: String?,
    val isOpaque: Boolean,
    val isActive: Boolean,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
) {

    fun isConfigured(): Boolean {
        return if (isOpaque) {
            issuer.isNotBlank()
        } else {
            issuer.isNotBlank() && !jwkSetUri.isNullOrBlank()
        }
    }
}
