package es.bdo.skeleton.tenant.application.model

import es.bdo.skeleton.tenant.domain.Config

data class ConfigDTO(
    val tenantId: String,
    val primaryColor: String?,
    val secondaryColor: String?,
    val logoUrl: String?
)

fun Config.toDTO(): ConfigDTO {
    return ConfigDTO(
        tenantId,
        primaryColor,
        secondaryColor,
        logoUrl
    )
}
