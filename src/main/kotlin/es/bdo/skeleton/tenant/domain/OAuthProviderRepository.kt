package es.bdo.skeleton.tenant.domain

interface OAuthProviderRepository {
    fun findAllByTenantId(tenantId: String): List<OAuthProvider>

    fun findActiveByTenantId(tenantId: String): List<OAuthProvider>

    fun findByTenantIdAndIssuer(tenantId: String, issuer: String): OAuthProvider?
}
