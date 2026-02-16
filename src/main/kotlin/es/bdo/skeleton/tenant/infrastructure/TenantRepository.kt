package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.domain.Tenant
import es.bdo.skeleton.tenant.infrastructure.model.toDomain
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import es.bdo.skeleton.tenant.domain.TenantRepository as ITenantRepository

@Repository
class TenantRepository(
    private val jpaRepository: TenantJpaRepository
) : ITenantRepository {

    @Cacheable(value = ["tenants"], key = "#all")
    override fun findAllActive(): List<Tenant> {
        return jpaRepository.findAllByIsActive(true)
            .map { it.toDomain() }
    }

    @Cacheable(value = ["tenants"], key = "#id.hashCode()")
    override fun findById(id: String): Tenant? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    @CacheEvict(value = ["tenants"], allEntries = true)
    override fun evictAll() {
    }

    @CacheEvict(value = ["tenants"], key = "#id.hashCode()")
    override fun evictCache(id: String) {
    }
}
