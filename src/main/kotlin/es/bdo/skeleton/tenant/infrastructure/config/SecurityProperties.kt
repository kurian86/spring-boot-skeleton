package es.bdo.skeleton.tenant.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security")
data class SecurityProperties(
    val encryption: Encryption
) {

    data class Encryption(
        val masterKey: String,
        val salt: String
    )
}
