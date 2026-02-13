package es.bdo.skeleton.tenant.infrastructure.model

import es.bdo.skeleton.tenant.domain.OAuthProvider

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

fun OAuthProvider.toEntity(): OAuthProviderEntity {
    return OAuthProviderEntity(
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
