package es.bdo.skeleton.user.application.service

import es.bdo.skeleton.user.application.exception.UserDisabledException
import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import es.bdo.skeleton.user.domain.UserStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserAuthorizationServiceTest {

    private val userRepository = mock<UserRepository>()
    private val service = UserAuthorizationService(userRepository)

    @Test
    fun `should return ROLE_USER when user exists and is active`() {
        // Given
        val email = "test@example.com"
        val user = User(
            id = UUID.randomUUID(),
            name = "Test User",
            email = email,
            status = UserStatus.ACTIVE
        )
        whenever(userRepository.findByEmail(email)).thenReturn(user)

        // When
        val authorities = service.loadUserAuthorities(email)

        // Then
        assertEquals(1, authorities.size)
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_USER")))
    }

    @Test
    fun `should return empty authorities when user does not exist`() {
        // Given
        val email = "unknown@example.com"
        whenever(userRepository.findByEmail(email)).thenReturn(null)

        // When
        val authorities = service.loadUserAuthorities(email)

        // Then
        assertTrue(authorities.isEmpty())
    }

    @Test
    fun `should throw UserDisabledException when user is disabled`() {
        // Given
        val email = "disabled@example.com"
        val user = User(
            id = UUID.randomUUID(),
            name = "Disabled User",
            email = email,
            status = UserStatus.DISABLED
        )
        whenever(userRepository.findByEmail(email)).thenReturn(user)

        // Then
        assertThrows<UserDisabledException> {
            service.loadUserAuthorities(email)
        }
    }
}
