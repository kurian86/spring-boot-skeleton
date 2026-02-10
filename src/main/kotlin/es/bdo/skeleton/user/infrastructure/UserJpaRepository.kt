package es.bdo.skeleton.user.infrastructure

import es.bdo.skeleton.user.infrastructure.model.UserEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserJpaRepository : CrudRepository<UserEntity, UUID>
