package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.domain.Tenant
import es.bdo.skeleton.tenant.infrastructure.model.toDomain
import org.springframework.stereotype.Repository
import es.bdo.skeleton.tenant.domain.TenantRepository as ITenantRepository

@Repository
class TenantRepository(
    private val jpaRepository: TenantJpaRepository
) : ITenantRepository {

    override fun findAllActive(): List<Tenant> {
        return jpaRepository.findAllByIsActive(true)
            .map { it.toDomain() }
    }

    override fun findById(id: String): Tenant? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }
}
