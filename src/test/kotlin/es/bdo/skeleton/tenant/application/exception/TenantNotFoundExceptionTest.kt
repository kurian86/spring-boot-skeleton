package es.bdo.skeleton.tenant.application.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TenantNotFoundExceptionTest {

    @Test
    fun `should create exception with default message`() {
        // Act
        val exception = TenantNotFoundException()

        // Assert
        assertThat(exception.message).isEqualTo("Tenant not found")
    }

    @Test
    fun `should create exception with custom message`() {
        // Arrange
        val customMessage = "Custom tenant error message"

        // Act
        val exception = TenantNotFoundException(customMessage)

        // Assert
        assertThat(exception.message).isEqualTo(customMessage)
    }

    @Test
    fun `should be runtime exception`() {
        // Act
        val exception = TenantNotFoundException()

        // Assert
        assertThat(exception).isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `should preserve message format with tenant id`() {
        // Arrange
        val tenantId = "tenant-123"
        val message = "Tenant with id $tenantId not found in database"

        // Act
        val exception = TenantNotFoundException(message)

        // Assert
        assertThat(exception.message).contains(tenantId)
        assertThat(exception.message).contains("not found")
    }
}
