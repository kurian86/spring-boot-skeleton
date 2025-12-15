package es.bdo.skeleton.main.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
class CatalogDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    fun catalogDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    fun catalogDataSource(): DataSource {
        return catalogDataSourceProperties().initializeDataSourceBuilder().build()
    }

    @Bean
    fun catalogJdbcTemplate(
        @Qualifier("catalogDataSource")
        catalogDataSource: DataSource
    ): JdbcTemplate {
        return JdbcTemplate(catalogDataSource)
    }
}
