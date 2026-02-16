package es.bdo.skeleton.user.application.model

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserStatus
import java.time.ZonedDateTime
import java.util.*

data class UserDTO(
    val id: UUID,
    val name: String,
    val email: String,
    val status: UserStatus = UserStatus.ACTIVE,
    val lastLoginAt: ZonedDateTime? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime = ZonedDateTime.now(),
)

fun User.toDTO() = UserDTO(
    id,
    name,
    email,
    status,
    lastLoginAt,
    createdAt,
    updatedAt
)
