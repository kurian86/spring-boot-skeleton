package es.bdo.skeleton.tenant.application

import es.bdo.skeleton.tenant.application.model.ConfigDTO
import es.bdo.skeleton.tenant.application.model.toDTO
import es.bdo.skeleton.tenant.domain.ConfigRepository
import org.springframework.stereotype.Service

@Service
class ConfigProvider(
    private val repository: ConfigRepository
) {

    fun find(): ConfigDTO? {
        val tenantId = TenantContext.tenantId ?: return null
        return repository.findByTenantId(tenantId)?.toDTO()
    }
}
