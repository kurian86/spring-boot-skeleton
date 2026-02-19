package es.bdo.skeleton.user.application

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import es.bdo.skeleton.user.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.ZonedDateTime
import java.util.UUID

class UserRegistrationServiceTest {

    private val repository: UserRepository = mock()
    private val service = UserRegistrationService(repository)

    private fun existingUser(email: String = "alice@example.com") = User(
        id = UUID.randomUUID(),
        name = "Alice",
        email = email,
        status = UserStatus.ACTIVE,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
    )

    @Test
    fun `ensureUserExists returns existing user when email is found`() {
        // Arrange
        val user = existingUser()
        whenever(repository.findByEmail(user.email)).thenReturn(user)

        // Act
        val result = service.ensureUserExists(user.email, "Alice", null)

        // Assert
        assertThat(result).isEqualTo(user)
        verify(repository, never()).save(any())
    }

    @Test
    fun `ensureUserExists does not call save when user already exists`() {
        // Arrange
        val user = existingUser()
        whenever(repository.findByEmail(user.email)).thenReturn(user)

        // Act
        service.ensureUserExists(user.email, "Alice", null)

        // Assert
        verify(repository, never()).save(any())
    }

    @Test
    fun `ensureUserExists creates and saves new user when email not found`() {
        // Arrange
        whenever(repository.findByEmail("new@example.com")).thenReturn(null)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] as User }

        // Act
        val result = service.ensureUserExists("new@example.com", "New User", null)

        // Assert
        verify(repository).save(any())
        assertThat(result.email).isEqualTo("new@example.com")
        assertThat(result.name).isEqualTo("New User")
    }

    @Test
    fun `ensureUserExists creates new user with ACTIVE status`() {
        // Arrange
        whenever(repository.findByEmail(any())).thenReturn(null)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] as User }

        // Act
        val result = service.ensureUserExists("x@example.com", "X", null)

        // Assert
        assertThat(result.status).isEqualTo(UserStatus.ACTIVE)
    }

    @Test
    fun `ensureUserExists propagates externalId to new user`() {
        // Arrange
        whenever(repository.findByEmail(any())).thenReturn(null)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] as User }

        // Act
        val result = service.ensureUserExists("x@example.com", "X", "ext-123")

        // Assert
        assertThat(result.externalId).isEqualTo("ext-123")
    }

    @Test
    fun `ensureUserExists propagates null externalId to new user`() {
        // Arrange
        whenever(repository.findByEmail(any())).thenReturn(null)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] as User }

        // Act
        val result = service.ensureUserExists("x@example.com", "X", null)

        // Assert
        assertThat(result.externalId).isNull()
    }
}
