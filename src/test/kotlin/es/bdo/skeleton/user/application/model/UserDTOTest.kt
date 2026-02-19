package es.bdo.skeleton.user.application.model

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID

class UserDTOTest {

    private val fixedTime = ZonedDateTime.now()
    private val userId = UUID.randomUUID()

    private fun user(
        status: UserStatus = UserStatus.ACTIVE,
        externalId: String? = null,
    ) = User(
        id = userId,
        name = "Alice",
        email = "alice@example.com",
        status = status,
        externalId = externalId,
        createdAt = fixedTime,
        updatedAt = fixedTime,
    )

    @Test
    fun `toDTO maps id correctly`() {
        assertThat(user().toDTO().id).isEqualTo(userId)
    }

    @Test
    fun `toDTO maps name correctly`() {
        assertThat(user().toDTO().name).isEqualTo("Alice")
    }

    @Test
    fun `toDTO maps email correctly`() {
        assertThat(user().toDTO().email).isEqualTo("alice@example.com")
    }

    @Test
    fun `toDTO maps ACTIVE status correctly`() {
        assertThat(user(status = UserStatus.ACTIVE).toDTO().status).isEqualTo(UserStatusDTO.ACTIVE)
    }

    @Test
    fun `toDTO maps DISABLED status correctly`() {
        assertThat(user(status = UserStatus.DISABLED).toDTO().status).isEqualTo(UserStatusDTO.DISABLED)
    }

    @Test
    fun `toDTO sets roles to ROLE_USER`() {
        assertThat(user().toDTO().roles).containsExactly("ROLE_USER")
    }

    @Test
    fun `toDTO maps createdAt correctly`() {
        assertThat(user().toDTO().createdAt).isEqualTo(fixedTime)
    }

    @Test
    fun `toDTO maps updatedAt correctly`() {
        assertThat(user().toDTO().updatedAt).isEqualTo(fixedTime)
    }
}
