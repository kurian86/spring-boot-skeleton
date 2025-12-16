package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.application.TenantContext
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource

class MultiTenantDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any? {
        return TenantContext.tenantId
    }
}
