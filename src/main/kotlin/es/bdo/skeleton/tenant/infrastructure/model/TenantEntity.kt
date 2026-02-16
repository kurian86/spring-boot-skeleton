package es.bdo.skeleton.tenant.infrastructure.model

import es.bdo.skeleton.tenant.domain.Tenant
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.ZonedDateTime

@Entity
@Table(name = "tenants")
data class TenantEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "db_database", nullable = false)
    val dbDatabase: String,

    @Column(name = "db_username", nullable = false)
    val dbUsername: String,

    @Column(name = "db_password", nullable = false)
    val dbPassword: String,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: ZonedDateTime = ZonedDateTime.now()
)

fun TenantEntity.toDomain(): Tenant {
    return Tenant(
        id,
        name,
        dbDatabase,
        dbUsername,
        dbPassword,
        isActive,
        createdAt,
        updatedAt
    )
}
