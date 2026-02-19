package es.bdo.skeleton.user.application.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.AuthenticationException

class UserDisabledExceptionTest {

    @Test
    fun `default message is set correctly`() {
        // Arrange + Act
        val ex = UserDisabledException()

        // Assert
        assertThat(ex.message).isEqualTo("User account is disabled")
    }

    @Test
    fun `custom message overrides default`() {
        // Arrange + Act
        val ex = UserDisabledException("Account suspended")

        // Assert
        assertThat(ex.message).isEqualTo("Account suspended")
    }

    @Test
    fun `is an AuthenticationException`() {
        // Arrange + Act
        val ex = UserDisabledException()

        // Assert
        assertThat(ex).isInstanceOf(AuthenticationException::class.java)
    }
}
