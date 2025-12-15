package es.bdo.skeleton.user.domain

import java.util.UUID

data class User(
    val id: UUID,
    val name: String,
    val email: String
)
