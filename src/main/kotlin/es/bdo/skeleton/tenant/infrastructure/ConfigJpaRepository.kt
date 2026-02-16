package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.infrastructure.model.ConfigEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface ConfigJpaRepository : CrudRepository<ConfigEntity, UUID> {
    fun findByTenantId(tenantId: String): Optional<ConfigEntity>
}
