package es.bdo.skeleton.user.infrastructure

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.infrastructure.model.toDomain
import es.bdo.skeleton.user.infrastructure.model.toEntity
import org.springframework.stereotype.Repository
import es.bdo.skeleton.user.domain.UserRepository as IUserRepository

@Repository
class UserRepository(
    private val jpaRepository: UserJpaRepository
) : IUserRepository {

    override fun findAll(): List<User> {
        return jpaRepository.findAll()
            .map { it.toDomain() }
    }

    override fun findByEmail(email: String): User? {
        return jpaRepository.findByEmail(email)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun save(user: User): User {
        val entity = user.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }
}
