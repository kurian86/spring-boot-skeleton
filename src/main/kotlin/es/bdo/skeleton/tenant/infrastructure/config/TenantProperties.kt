package es.bdo.skeleton.tenant.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.tenant")
data class TenantProperties(
    val cache: Cache,
    val datasource: Datasource
) {

    data class Cache(
        val ttlSeconds: Long = 300
    )

    data class Datasource(
        val urlTemplate: String,
        val driverClassName: String,
        val maximumPoolSize: Int,
        val minimumIdle: Int
    )
}
