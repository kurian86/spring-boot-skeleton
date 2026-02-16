package es.bdo.skeleton.user.application.service

import es.bdo.skeleton.user.application.exception.UserDisabledException
import es.bdo.skeleton.user.domain.UserRepository
import es.bdo.skeleton.user.domain.UserStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service

@Service
class UserAuthorizationService(
    private val userRepository: UserRepository
) {

    fun loadUserAuthorities(email: String): Collection<GrantedAuthority> {
        val user = userRepository.findByEmail(email)
            ?: return emptyList()

        if (user.status == UserStatus.DISABLED) {
            throw UserDisabledException()
        }

        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }
}
