package es.bdo.skeleton.tenant.infrastructure.migration

import es.bdo.skeleton.tenant.infrastructure.config.TenantProperties
import es.bdo.skeleton.tenant.infrastructure.service.TenantConfigurationService
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class FlywayTenantMigrator(
    private val properties: TenantProperties,
    private val tenantConfigService: TenantConfigurationService
) : CommandLineRunner {

    override fun run(vararg args: String) {
        val tenantDataSources = tenantConfigService.getAllActiveDataSources()

        if (tenantDataSources.isEmpty() || !properties.flyway.enabled) {
            return
        }

        tenantDataSources.forEach { (tenantId, tenantDataSource) ->
            try {
                Flyway.configure()
                    .dataSource(tenantDataSource)
                    .locations(properties.flyway.locations)
                    .baselineOnMigrate(properties.flyway.baselineOnMigrate)
                    .failOnMissingLocations(properties.flyway.failOnMissingLocations)
                    .load()
                    .migrate()
            } catch (e: FlywayException) {
                throw IllegalStateException("Database migration failed for tenant '$tenantId'. Check the schema state.", e)
            }
        }
    }
}
