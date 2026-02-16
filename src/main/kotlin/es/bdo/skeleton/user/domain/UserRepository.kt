package es.bdo.skeleton.user.domain

interface UserRepository {
    fun findAll(): List<User>

    fun findByEmail(email: String): User?

    fun save(user: User): User
}
