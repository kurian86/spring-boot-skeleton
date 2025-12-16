package es.bdo.skeleton.tenant.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.flyway")
data class FlywayProperties(
    val locations: String
)
