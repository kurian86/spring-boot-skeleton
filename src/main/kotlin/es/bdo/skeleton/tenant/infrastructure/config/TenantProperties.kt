package es.bdo.skeleton.tenant.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.tenant")
data class TenantProperties(
    val cache: Cache,
    val datasource: Datasource,
    val flyway: Flyway
) {

    data class Cache(
        val ttlSeconds: Long = 300
    )

    data class Datasource(
        val urlTemplate: String,
        val driverClassName: String,
        val maximumPoolSize: Int,
        val minimumIdle: Int,
        val maxLifetime: Long? = 300000L,
        val idleTimeout: Long? = 120000L,
        val connectionTimeout: Long? = 30000L,
        val keepaliveTime: Long? = 60000L
    )

    data class Flyway(
        val enabled: Boolean,
        val locations: String,
        val baselineOnMigrate: Boolean,
        val failOnMissingLocations: Boolean,
    )
}
