package es.bdo.skeleton.main.tenant

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Service
class TenantConfigurationService(
    @Qualifier("catalogJdbcTemplate")
    private val jdbcTemplate: JdbcTemplate,
    @Value($$"${app.tenant.datasource.driver-class-name}")
    private val driverClassName: String,
    @Value($$"${app.tenant.datasource.maximum-pool-size}")
    private val maximumPoolSize: Int,
    @Value($$"${app.tenant.datasource.minimum-idle}")
    private val minimumIdle: Int
) {

    private val tenantDataSources = mutableMapOf<String, DataSource>()

    fun loadAllTenantConfigs(): Map<String, DataSource> {
        val sql = "SELECT tenant_id, db_url, db_username, db_password FROM tenants_configs WHERE is_active = true"

        val configs = jdbcTemplate.query(sql) { rs, _ ->
            TenantConfig(
                tenantId = rs.getString("tenant_id"),
                dbUrl = rs.getString("db_url"),
                dbUsername = rs.getString("db_username"),
                dbPassword = rs.getString("db_password")
            )
        }

        configs.forEach { config ->
            tenantDataSources[config.tenantId] = createTenantDataSource(config)
        }

        return tenantDataSources
    }

    fun getDataSourceForTenant(tenantId: String): DataSource? {
        return tenantDataSources[tenantId]
    }

    private fun createTenantDataSource(config: TenantConfig): DataSource {
        return HikariDataSource().apply {
            poolName = "Tenant-${config.tenantId}-Pool"
            jdbcUrl = config.dbUrl
            username = config.dbUsername
            password = config.dbPassword
            driverClassName = this@TenantConfigurationService.driverClassName
            maximumPoolSize = this@TenantConfigurationService.maximumPoolSize
            minimumIdle = this@TenantConfigurationService.minimumIdle
        }
    }

    fun addTenantDataSource(config: TenantConfig): DataSource {
        val newDs = createTenantDataSource(config)
        tenantDataSources[config.tenantId] = newDs
        return newDs
    }

    fun getAllActiveDataSources(): Map<String, DataSource> {
        return tenantDataSources.toMap()
    }
}
