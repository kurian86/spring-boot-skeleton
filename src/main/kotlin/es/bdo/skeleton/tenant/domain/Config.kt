package es.bdo.skeleton.tenant.domain

import java.util.UUID

data class Config(
    val id: UUID,
    val tenantId: String,
    val allowedDomains: List<String>,
    val primaryColor: String?,
    val secondaryColor: String?,
    val logoUrl: String?
)
