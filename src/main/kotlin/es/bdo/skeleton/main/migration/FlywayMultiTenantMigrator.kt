package es.bdo.skeleton.main.migration

import es.bdo.skeleton.main.tenant.TenantConfigurationService
import org.flywaydb.core.Flyway
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class FlywayMultiTenantMigrator(
    private val tenantConfigService: TenantConfigurationService
) : CommandLineRunner {

    private val tenantMigrationLocation = "classpath:db/migration/tenant"

    override fun run(vararg args: String) {
        val tenantDataSources = tenantConfigService.getAllActiveDataSources()

        if (tenantDataSources.isEmpty()) {
            return
        }

        tenantDataSources.forEach { (tenantId, tenantDataSource) ->
            Flyway.configure()
                .dataSource(tenantDataSource)
                .locations(tenantMigrationLocation)
                .baselineOnMigrate(true)
                .load()
                .migrate()
        }
    }
}
