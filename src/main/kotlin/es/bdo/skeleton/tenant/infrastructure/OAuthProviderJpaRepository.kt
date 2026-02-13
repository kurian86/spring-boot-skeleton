package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.infrastructure.model.OAuthProviderEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface OAuthProviderJpaRepository : CrudRepository<OAuthProviderEntity, UUID> {
    fun findAllByTenantId(tenantId: String): List<OAuthProviderEntity>

    fun findAllByTenantIdAndIsActive(tenantId: String, isActive: Boolean): List<OAuthProviderEntity>

    fun findByTenantIdAndIssuer(tenantId: String, issuer: String): Optional<OAuthProviderEntity>
}
