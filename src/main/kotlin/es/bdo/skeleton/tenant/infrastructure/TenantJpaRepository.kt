package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.infrastructure.model.TenantEntity
import org.springframework.data.repository.CrudRepository

interface TenantJpaRepository : CrudRepository<TenantEntity, String> {
    fun findAllByIsActive(isActive: Boolean): List<TenantEntity>
}
