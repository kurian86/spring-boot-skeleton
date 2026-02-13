package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.domain.OAuthProvider
import es.bdo.skeleton.tenant.infrastructure.model.toDomain
import org.springframework.stereotype.Repository
import es.bdo.skeleton.tenant.domain.OAuthProviderRepository as IOAuthProviderRepository

@Repository
class OAuthProviderRepository(
    private val jpaRepository: OAuthProviderJpaRepository
) : IOAuthProviderRepository {

    override fun findAllByTenantId(tenantId: String): List<OAuthProvider> {
        return jpaRepository.findAllByTenantId(tenantId)
            .map { it.toDomain() }
    }

    override fun findActiveByTenantId(tenantId: String): List<OAuthProvider> {
        return jpaRepository.findAllByTenantIdAndIsActive(tenantId, true)
            .map { it.toDomain() }
    }

    override fun findByTenantIdAndIssuer(tenantId: String, issuer: String): OAuthProvider? {
        return jpaRepository.findByTenantIdAndIssuer(tenantId, issuer)
            .map { it.toDomain() }
            .orElse(null)
    }
}
