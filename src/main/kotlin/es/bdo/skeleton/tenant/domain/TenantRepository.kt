package es.bdo.skeleton.tenant.domain

interface TenantRepository {
    fun findAllActive(): List<Tenant>

    fun findById(id: String): Tenant?

    fun evictCache()

    fun evictCache(id: String)
}
