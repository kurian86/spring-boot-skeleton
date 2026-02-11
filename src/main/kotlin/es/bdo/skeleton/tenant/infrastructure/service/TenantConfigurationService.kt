package es.bdo.skeleton.tenant.infrastructure.service

import com.zaxxer.hikari.HikariDataSource
import es.bdo.skeleton.tenant.application.TenantProvider
import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
import es.bdo.skeleton.tenant.domain.Tenant
import es.bdo.skeleton.tenant.infrastructure.config.TenantProperties
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

@Service
class TenantConfigurationService(
    private val properties: TenantProperties,
    private val provider: TenantProvider,
    private val encryptionService: EncryptionService
) {

    private val dataSources = ConcurrentHashMap<String, DataSource>()

    init {
        loadAllTenants()
    }

    fun loadAllTenants(): Map<String, DataSource> {
        val tenants = provider.findAllActive()

        tenants.forEach { tenant ->
            dataSources[tenant.id] = createDataSource(tenant)
        }

        return dataSources
    }

    fun getOrCreateDataSource(tenantId: String): DataSource {
        var dataSource = dataSources[tenantId]

        if (dataSource == null) {
            val tenant = provider.findById(tenantId)
                ?: throw TenantNotFoundException("Tenant not found: $tenantId")

            if (!tenant.isActive) {
                evictDataSource(tenantId)
                throw TenantNotFoundException("Tenant is not active: $tenantId")
            }

            dataSource = addDataSource(tenant)
        }

        return dataSource
    }

    fun addDataSource(tenant: Tenant): DataSource {
        val newDs = createDataSource(tenant)
        dataSources[tenant.id] = newDs
        return newDs
    }

    private fun createDataSource(tenant: Tenant): DataSource {
        return HikariDataSource().apply {
            poolName = "Tenant-${tenant.id}-Pool"
            jdbcUrl = properties.datasource.urlTemplate.format(tenant.dbDatabase)
            username = tenant.dbUsername
            password = encryptionService.decrypt(tenant.dbPassword)
            driverClassName = properties.datasource.driverClassName
            maximumPoolSize = properties.datasource.maximumPoolSize
            minimumIdle = properties.datasource.minimumIdle
            maxLifetime = properties.datasource.maxLifetime ?: 300000L
            idleTimeout = properties.datasource.idleTimeout ?: 120000L
            connectionTimeout = properties.datasource.connectionTimeout ?: 30000L
            keepaliveTime = properties.datasource.keepaliveTime ?: 60000L
        }
    }

    fun evictDataSource(tenantId: String) {
        dataSources.remove(tenantId)
        provider.evictTenant(tenantId)
    }

    fun getAllActiveDataSources(): Map<String, DataSource> {
        return dataSources.toMap()
    }

    fun closeAll() {
        getAllActiveDataSources().forEach { (_, ds) ->
            if (ds is HikariDataSource) {
                ds.close()
            }
        }
    }
}
