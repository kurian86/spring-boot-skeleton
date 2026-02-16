package es.bdo.skeleton.user.application.model

import es.bdo.skeleton.user.domain.User
import java.time.ZonedDateTime
import java.util.*

data class UserDTO(
    val id: UUID,
    val name: String,
    val email: String,
    val status: UserStatusDTO = UserStatusDTO.ACTIVE,
    val roles: Set<String> = emptySet(),
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime = ZonedDateTime.now(),
)

fun User.toDTO() = UserDTO(
    id,
    name,
    email,
    status.toDTO(),
    setOf("ROLE_USER"),
    createdAt,
    updatedAt
)
