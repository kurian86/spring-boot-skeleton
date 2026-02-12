package es.bdo.skeleton.tenant.domain

interface OAuthProviderRepository {
    fun findAllByTenantId(tenantId: String): List<OAuthProvider>

    fun findActiveByTenantId(tenantId: String): List<OAuthProvider>

    fun findByTenantIdAndType(tenantId: String, providerType: ProviderType): OAuthProvider?
}
