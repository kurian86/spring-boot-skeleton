package es.bdo.skeleton.user.domain

interface IUserRepository {
    fun findAll(): List<User>
}
