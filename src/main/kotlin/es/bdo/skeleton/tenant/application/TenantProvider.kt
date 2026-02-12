package es.bdo.skeleton.tenant.application

import es.bdo.skeleton.tenant.domain.TenantRepository
import es.bdo.skeleton.tenant.domain.Tenant
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class TenantProvider(
    private val tenantRepository: TenantRepository
) {

    fun findAllActive(): List<Tenant> {
        return tenantRepository.findAllActive()
    }

    @Cacheable(value = ["tenants"], key = "#tenantId.hashCode()")
    fun findById(tenantId: String): Tenant? {
        return tenantRepository.findById(tenantId)
    }

    @CacheEvict(value = ["tenants"], key = "#tenantId.hashCode()")
    fun evictTenant(tenantId: String) {
    }

    @CacheEvict(value = ["tenants"], allEntries = true)
    fun evictAll() {
    }
}
