package es.bdo.skeleton.user.application.model

import es.bdo.skeleton.user.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserStatusDTOTest {

    @Test
    fun `ACTIVE maps to ACTIVE`() {
        // Arrange + Act
        val result = UserStatus.ACTIVE.toDTO()

        // Assert
        assertThat(result).isEqualTo(UserStatusDTO.ACTIVE)
    }

    @Test
    fun `DISABLED maps to DISABLED`() {
        // Arrange + Act
        val result = UserStatus.DISABLED.toDTO()

        // Assert
        assertThat(result).isEqualTo(UserStatusDTO.DISABLED)
    }

    @Test
    fun `all UserStatus values have a DTO mapping`() {
        // Arrange + Act + Assert
        UserStatus.entries.forEach { status ->
            assertThat(status.toDTO()).isNotNull()
        }
    }
}
