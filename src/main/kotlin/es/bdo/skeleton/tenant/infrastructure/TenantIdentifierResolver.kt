package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.application.TenantContext
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.springframework.stereotype.Component

@Component
class TenantIdentifierResolver : CurrentTenantIdentifierResolver<String> {

    companion object {
        const val DEFAULT_TENANT = "default"
    }

    override fun resolveCurrentTenantIdentifier(): String {
        return TenantContext.tenantId ?: DEFAULT_TENANT
    }

    override fun validateExistingCurrentSessions(): Boolean {
        return true
    }
}
