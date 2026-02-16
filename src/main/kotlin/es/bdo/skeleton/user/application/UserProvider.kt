package es.bdo.skeleton.user.application

import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.model.toDTO
import es.bdo.skeleton.user.domain.UserRepository
import org.springframework.stereotype.Service

@Service
class UserProvider(
    private val userRepository: UserRepository,
) {

    fun findAll(): List<UserDTO> {
        return userRepository.findAll()
            .map { it.toDTO() }
    }
}
