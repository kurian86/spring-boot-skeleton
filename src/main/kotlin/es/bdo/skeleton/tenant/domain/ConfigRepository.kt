package es.bdo.skeleton.tenant.domain

interface ConfigRepository {
    fun findByTenantId(tenantId: String): Config?
}
