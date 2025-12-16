package es.bdo.skeleton.tenant.infrastructure

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource

class MultiTenantDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any? {
        return TenantContext.tenantId
    }
}
