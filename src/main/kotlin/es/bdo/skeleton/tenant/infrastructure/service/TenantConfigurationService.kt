package es.bdo.skeleton.tenant.infrastructure.service

import com.zaxxer.hikari.HikariDataSource
import es.bdo.skeleton.tenant.application.TenantProvider
import es.bdo.skeleton.tenant.domain.Tenant
import es.bdo.skeleton.tenant.infrastructure.config.TenantProperties
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Service
class TenantConfigurationService(
    private val properties: TenantProperties,
    private val provider: TenantProvider,
    private val encryptionService: EncryptionService
) {

    private val dataSources = mutableMapOf<String, DataSource>()

    fun loadAllTenants(): Map<String, DataSource> {
        val tenants = provider.findAllActive()

        tenants.forEach { tenant ->
            dataSources[tenant.id] = createDataSource(tenant)
        }

        return dataSources
    }

    fun getDataSource(id: String): DataSource? {
        return dataSources[id]
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
        }
    }

    fun addDataSource(tenant: Tenant): DataSource {
        val newDs = createDataSource(tenant)
        dataSources[tenant.id] = newDs
        return newDs
    }

    fun getAllActiveDataSources(): Map<String, DataSource> {
        return dataSources.toMap()
    }
}