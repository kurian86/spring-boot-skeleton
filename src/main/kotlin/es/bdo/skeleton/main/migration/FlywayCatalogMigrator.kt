package es.bdo.skeleton.main.migration

import jakarta.annotation.PostConstruct
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import javax.sql.DataSource

@Configuration
@DependsOn("catalogDataSource")
class FlywayCatalogMigrator(
    @Qualifier("catalogDataSource")
    private val catalogDataSource: DataSource,
    @Value($$"${spring.flyway.locations}")
    private val catalogMigrationLocation: String
) {

    @PostConstruct
    fun migrateCatalog() {
        Flyway.configure()
            .dataSource(catalogDataSource)
            .locations(catalogMigrationLocation.split(",")[0].trim())
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}
