package es.bdo.skeleton.user.infrastructure

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository

class UserRepositoryImpl : UserRepository {
    override fun findAll() = listOf<User>()
}
