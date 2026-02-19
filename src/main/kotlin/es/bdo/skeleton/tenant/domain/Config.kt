package es.bdo.skeleton.tenant.domain

import java.util.*

data class Config(
    val id: UUID,
    val tenantId: String,
    val primaryColor: String?,
    val secondaryColor: String?,
    val logoUrl: String?
)
