package es.bdo.skeleton.user.infrastructure

import es.bdo.skeleton.user.domain.UserRepository as IUserRepository
import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.infrastructure.model.toDomain
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val jpaRepository: UserJpaRepository
) : IUserRepository {

    override fun findAll(): List<User> {
        return jpaRepository.findAll()
            .map { it.toDomain() }
    }
}
