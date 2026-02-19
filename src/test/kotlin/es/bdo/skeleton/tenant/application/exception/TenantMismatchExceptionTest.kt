package es.bdo.skeleton.tenant.application.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TenantMismatchExceptionTest {

    @Test
    fun `should create exception with both tenant ids`() {
        // Arrange
        val jwtTenantId = "jwt-tenant-123"
        val contextTenantId = "context-tenant-456"

        // Act
        val exception = TenantMismatchException(jwtTenantId, contextTenantId)

        // Assert
        assertThat(exception.jwtTenantId).isEqualTo(jwtTenantId)
        assertThat(exception.contextTenantId).isEqualTo(contextTenantId)
        assertThat(exception.message).contains(jwtTenantId)
        assertThat(exception.message).contains(contextTenantId)
    }

    @Test
    fun `should handle null jwt tenant id`() {
        // Arrange
        val contextTenantId = "context-tenant"

        // Act
        val exception = TenantMismatchException(null, contextTenantId)

        // Assert
        assertThat(exception.jwtTenantId).isNull()
        assertThat(exception.contextTenantId).isEqualTo(contextTenantId)
        assertThat(exception.message).contains("null")
    }

    @Test
    fun `should handle null context tenant id`() {
        // Arrange
        val jwtTenantId = "jwt-tenant"

        // Act
        val exception = TenantMismatchException(jwtTenantId, null)

        // Assert
        assertThat(exception.jwtTenantId).isEqualTo(jwtTenantId)
        assertThat(exception.contextTenantId).isNull()
        assertThat(exception.message).contains("null")
    }

    @Test
    fun `should handle both tenant ids as null`() {
        // Act
        val exception = TenantMismatchException(null, null)

        // Assert
        assertThat(exception.jwtTenantId).isNull()
        assertThat(exception.contextTenantId).isNull()
        assertThat(exception.message).contains("JWT tenant_id")
        assertThat(exception.message).contains("context tenant")
    }

    @Test
    fun `should be runtime exception`() {
        // Act
        val exception = TenantMismatchException("a", "b")

        // Assert
        assertThat(exception).isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `should include tenant ids in error message`() {
        // Arrange
        val jwtTenantId = "tenant-jwt"
        val contextTenantId = "tenant-context"

        // Act
        val exception = TenantMismatchException(jwtTenantId, contextTenantId)

        // Assert
        assertThat(exception.message).isEqualTo(
            "JWT tenant_id '$jwtTenantId' does not match context tenant '$contextTenantId'"
        )
    }
}
