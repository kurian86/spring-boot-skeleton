package es.bdo.skeleton.user.domain

interface UserRepository {
    fun findAll(): List<User>
}
