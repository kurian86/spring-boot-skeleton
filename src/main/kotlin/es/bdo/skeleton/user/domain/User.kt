package es.bdo.skeleton.user.domain

import java.time.ZonedDateTime
import java.util.*

data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime = ZonedDateTime.now(),
)
