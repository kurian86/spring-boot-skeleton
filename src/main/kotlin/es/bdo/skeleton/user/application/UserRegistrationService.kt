package es.bdo.skeleton.user.application

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import es.bdo.skeleton.user.domain.UserStatus
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.UUID

@Service
class UserRegistrationService(
    private val userRepository: UserRepository,
) {

    /**
     * Ensures a user exists in the database. If the user doesn't exist,
     * creates a new user with the provided details.
     *
     * @param email User's email address
     * @param name User's display name
     * @param externalId External ID from the authentication provider (e.g., JWT sub claim)
     * @return The existing or newly created user
     */
    fun ensureUserExists(email: String, name: String, externalId: String?): User {
        return userRepository.findByEmail(email) ?: run {
            val newUser = User(
                id = UUID.randomUUID(),
                name = name,
                email = email,
                status = UserStatus.ACTIVE,
                externalId = externalId,
                createdAt = ZonedDateTime.now(),
                updatedAt = ZonedDateTime.now()
            )
            userRepository.save(newUser)
        }
    }

    /**
     * Finds a user by email address.
     *
     * @param email User's email address
     * @return The user if found, null otherwise
     */
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
}
