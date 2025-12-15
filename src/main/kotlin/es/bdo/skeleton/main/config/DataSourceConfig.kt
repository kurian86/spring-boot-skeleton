package es.bdo.skeleton.main.config

import es.bdo.skeleton.main.tenant.MultiTenantDataSource
import es.bdo.skeleton.main.tenant.TenantConfigurationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class DataSourceConfig(
    private val tenantConfigService: TenantConfigurationService
) {

    @Bean
    @Primary
    fun dataSource(): DataSource {
        val activeDataSources = tenantConfigService.loadAllTenantConfigs()
        if (activeDataSources.isEmpty()) {
            throw IllegalStateException("No active tenants found in the catalog database.")
        }

        val defaultTenantId = activeDataSources.keys.first()
        val routingDataSource = MultiTenantDataSource()

        routingDataSource.setTargetDataSources(activeDataSources.toMap())
        routingDataSource.setDefaultTargetDataSource(activeDataSources.getValue(defaultTenantId))
        routingDataSource.afterPropertiesSet()

        return routingDataSource
    }
}
