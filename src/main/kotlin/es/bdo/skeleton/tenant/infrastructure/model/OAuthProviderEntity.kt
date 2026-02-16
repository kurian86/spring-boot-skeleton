package es.bdo.skeleton.tenant.infrastructure.model

import es.bdo.skeleton.tenant.domain.OAuthProvider
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "tenants_oauth_providers")
data class OAuthProviderEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "issuer", nullable = false)
    val issuer: String,

    @Column(name = "jwk_set_uri")
    val jwkSetUri: String? = null,

    @Column(name = "is_opaque", nullable = false)
    val isOpaque: Boolean = false,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: ZonedDateTime = ZonedDateTime.now()
)

fun OAuthProviderEntity.toDomain(): OAuthProvider {
    return OAuthProvider(
        id,
        tenantId,
        name,
        issuer,
        jwkSetUri,
        isOpaque,
        isActive,
        createdAt,
        updatedAt
    )
}

