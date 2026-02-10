package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.infrastructure.service.TenantConfigurationService
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider
import org.springframework.stereotype.Component
import java.sql.Connection
import javax.sql.DataSource

@Component
class TenantConnectionProvider(
    private val catalogDataSource: DataSource,
    private val tenantConfigurationService: TenantConfigurationService
) : MultiTenantConnectionProvider<String> {

    override fun getAnyConnection(): Connection {
        return catalogDataSource.connection
    }

    override fun releaseAnyConnection(connection: Connection) {
        connection.close()
    }

    override fun getConnection(tenantIdentifier: String): Connection {
        return tenantConfigurationService.getOrCreateDataSource(tenantIdentifier).connection
    }

    override fun releaseConnection(tenantIdentifier: String, connection: Connection) {
        connection.close()
    }

    override fun supportsAggressiveRelease(): Boolean {
        return false
    }

    override fun isUnwrappableAs(unwrapType: Class<*>?): Boolean {
        return false
    }

    override fun <T : Any> unwrap(unwrapType: Class<T>): T {
        throw UnsupportedOperationException("Cannot unwrap TenantConnectionProvider to ${unwrapType.name}")
    }
}
