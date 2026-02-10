package es.bdo.skeleton.user.infrastructure.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
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
    val email: String
)
