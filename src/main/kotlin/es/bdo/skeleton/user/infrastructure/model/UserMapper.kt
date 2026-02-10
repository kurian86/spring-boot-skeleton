package es.bdo.skeleton.user.infrastructure.model

import es.bdo.skeleton.user.domain.User

fun UserEntity.toDomain() = User(
    id,
    name,
    email
)

fun User.toEntity() = UserEntity(
    id,
    name,
    email
)
