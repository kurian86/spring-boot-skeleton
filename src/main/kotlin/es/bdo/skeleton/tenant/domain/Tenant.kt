package es.bdo.skeleton.tenant.domain

import java.time.ZonedDateTime

data class Tenant(
    val id: String,
    val name: String,
    val dbDatabase: String,
    val dbUsername: String,
    val dbPassword: String,
    val isActive: Boolean,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
)
