package es.bdo.skeleton.tenant.infrastructure.model

import es.bdo.skeleton.tenant.domain.Tenant

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

fun Tenant.toEntity(): TenantEntity {
    return TenantEntity(
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
