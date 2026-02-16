package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.domain.Config
import es.bdo.skeleton.tenant.infrastructure.model.toDomain
import org.springframework.stereotype.Repository
import es.bdo.skeleton.tenant.domain.ConfigRepository as IConfigRepository

@Repository
class ConfigRepository(
    private val jpaRepository: ConfigJpaRepository
) : IConfigRepository {

    override fun findByTenantId(tenantId: String): Config? {
        return jpaRepository.findByTenantId(tenantId)
            .map { it.toDomain() }
            .orElse(null)
    }
}
