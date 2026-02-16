package es.bdo.skeleton.tenant.infrastructure.model

import es.bdo.skeleton.tenant.domain.Config
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tenants_config")
data class ConfigEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: String,

    @Column(name = "allowed_domains", nullable = false, columnDefinition = "TEXT")
    val allowedDomains: String,

    @Column(name = "primary_color")
    val primaryColor: String? = null,

    @Column(name = "secondary_color")
    val secondaryColor: String? = null,

    @Column(name = "logo_url")
    val logoUrl: String? = null
)

fun ConfigEntity.toDomain(): Config {
    return Config(
        id,
        tenantId,
        allowedDomains.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        primaryColor,
        secondaryColor,
        logoUrl
    )
}
