package es.bdo.skeleton.user.application

import es.bdo.skeleton.shared.extension.newUUID
import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import es.bdo.skeleton.user.domain.UserStatus
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class UserRegistrationService(
    private val userRepository: UserRepository,
) {

    fun ensureUserExists(email: String, name: String, externalId: String?): User {
        return userRepository.findByEmail(email) ?: run {
            val newUser = User(
                newUUID(),
                name,
                email,
                UserStatus.ACTIVE,
                externalId,
                ZonedDateTime.now(),
                ZonedDateTime.now()
            )
            userRepository.save(newUser)
        }
    }
}
