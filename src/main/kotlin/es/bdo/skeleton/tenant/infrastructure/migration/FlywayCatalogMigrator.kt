package es.bdo.skeleton.tenant.infrastructure.migration

import es.bdo.skeleton.tenant.infrastructure.config.FlywayProperties
import jakarta.annotation.PostConstruct
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayCatalogMigrator(
    @Qualifier("catalogDataSource")
    private val catalogDataSource: DataSource,
    private val flywayProperties: FlywayProperties
) {

    @PostConstruct
    fun migrateCatalog() {
        Flyway.configure()
            .dataSource(catalogDataSource)
            .locations(flywayProperties.locations.split(",")[0].trim())
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}
