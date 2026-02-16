package es.bdo.skeleton.user.application.model

import es.bdo.skeleton.user.domain.UserStatus

enum class UserStatusDTO {
    ACTIVE,
    DISABLED
}

fun UserStatus.toDTO(): UserStatusDTO {
    return when (this) {
        UserStatus.ACTIVE -> UserStatusDTO.ACTIVE
        UserStatus.DISABLED -> UserStatusDTO.DISABLED
    }
}
