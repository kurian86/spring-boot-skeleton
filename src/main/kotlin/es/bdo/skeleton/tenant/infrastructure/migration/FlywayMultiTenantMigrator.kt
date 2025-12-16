package es.bdo.skeleton.tenant.infrastructure.migration

import es.bdo.skeleton.tenant.infrastructure.TenantConfigurationService
import org.flywaydb.core.Flyway
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class FlywayMultiTenantMigrator(
    private val tenantConfigService: TenantConfigurationService
) : CommandLineRunner {

    private val migrationLocation = "classpath:db/migration/tenant"

    override fun run(vararg args: String) {
        val tenantDataSources = tenantConfigService.getAllActiveDataSources()

        if (tenantDataSources.isEmpty()) {
            return
        }

        tenantDataSources.forEach { (_, tenantDataSource) ->
            Flyway.configure()
                .dataSource(tenantDataSource)
                .locations(migrationLocation)
                .baselineOnMigrate(true)
                .load()
                .migrate()
        }
    }
}
