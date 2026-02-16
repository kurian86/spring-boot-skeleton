package es.bdo.skeleton.tenant.application.model

import es.bdo.skeleton.tenant.domain.Config
import java.util.UUID

data class ConfigDTO(
    val id: UUID,
    val tenantId: String,
    val allowedDomains: List<String>,
    val primaryColor: String?,
    val secondaryColor: String?,
    val logoUrl: String?
)

fun Config.toDTO(): ConfigDTO {
    return ConfigDTO(
        id,
        tenantId,
        allowedDomains,
        primaryColor,
        secondaryColor,
        logoUrl
    )
}
