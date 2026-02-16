package es.bdo.skeleton.user.application

import es.bdo.skeleton.user.application.exception.UserDisabledException
import es.bdo.skeleton.user.application.model.UserStatusDTO
import es.bdo.skeleton.user.application.model.toDTO
import es.bdo.skeleton.user.domain.UserRepository
import org.springframework.stereotype.Service

@Service
class UserProvider(
    private val userRepository: UserRepository,
) {

    fun findUserAuthoritiesByEmail(email: String): List<String> {
        val user = userRepository.findByEmail(email)?.toDTO()
            ?: return emptyList()

        if (user.status == UserStatusDTO.DISABLED) {
            throw UserDisabledException()
        }

        return user.roles.toList()
    }
}
