package es.bdo.skeleton.user.infrastructure

import es.bdo.skeleton.user.infrastructure.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.*

interface UserJpaRepository : JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    fun findByEmail(email: String): Optional<UserEntity>
}
