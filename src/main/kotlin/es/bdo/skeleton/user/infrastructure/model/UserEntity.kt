package es.bdo.skeleton.user.infrastructure.model

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserStatus
import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "email", nullable = false)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: UserStatus = UserStatus.ACTIVE,

    @Column(name = "last_login_at")
    val lastLoginAt: ZonedDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: ZonedDateTime = ZonedDateTime.now(),
)

fun UserEntity.toDomain() = User(
    id,
    name,
    email,
    status,
    lastLoginAt,
    createdAt,
    updatedAt,
)

fun User.toEntity() = UserEntity(
    id,
    name,
    email,
    status,
    lastLoginAt,
    createdAt,
    updatedAt,
)
