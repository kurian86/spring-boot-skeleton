package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.infrastructure.service.TenantConfigurationService
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Component

@Component
class TenantDataSourceShutdownHook(
    private val tenantConfigurationService: TenantConfigurationService
) {

    @PreDestroy
    fun onShutdown() {
        tenantConfigurationService.closeAll()
    }
}
