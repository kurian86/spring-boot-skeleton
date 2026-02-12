package es.bdo.skeleton.user.application

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import org.springframework.stereotype.Service

@Service
class UserProvider(
    private val userRepository: UserRepository,
) {

    fun findAll(): List<User> {
        return userRepository.findAll()
    }
}
