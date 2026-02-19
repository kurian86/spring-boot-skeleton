package es.bdo.skeleton.shared.exception

import es.bdo.skeleton.tenant.application.exception.TenantMismatchException
import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleTenantNotFound should return UNAUTHORIZED status`() {
        // Arrange
        val exception = TenantNotFoundException()

        // Act
        val response = handler.handleTenantNotFound(exception)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `handleTenantNotFound should return error response with default message`() {
        // Arrange
        val exception = TenantNotFoundException()

        // Act
        val response = handler.handleTenantNotFound(exception)
        val body = response.body!!

        // Assert
        assertThat(body.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
        assertThat(body.error).isEqualTo("Unauthorized")
        assertThat(body.message).isEqualTo("Tenant not found")
    }

    @Test
    fun `handleTenantNotFound should return error response with custom message`() {
        // Arrange
        val customMessage = "Custom tenant error message"
        val exception = TenantNotFoundException(customMessage)

        // Act
        val response = handler.handleTenantNotFound(exception)
        val body = response.body!!

        // Assert
        assertThat(body.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
        assertThat(body.error).isEqualTo("Unauthorized")
        assertThat(body.message).isEqualTo(customMessage)
    }

    @Test
    fun `handleTenantMismatch should return FORBIDDEN status`() {
        // Arrange
        val exception = TenantMismatchException("jwt-tenant", "context-tenant")

        // Act
        val response = handler.handleTenantMismatch(exception)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `handleTenantMismatch should return error response with correct status`() {
        // Arrange
        val exception = TenantMismatchException("jwt-tenant", "context-tenant")

        // Act
        val response = handler.handleTenantMismatch(exception)
        val body = response.body!!

        // Assert
        assertThat(body.status).isEqualTo(HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun `handleTenantMismatch should return error response with correct error type`() {
        // Arrange
        val exception = TenantMismatchException("jwt-tenant", "context-tenant")

        // Act
        val response = handler.handleTenantMismatch(exception)
        val body = response.body!!

        // Assert
        assertThat(body.error).isEqualTo("Forbidden")
    }

    @Test
    fun `handleTenantMismatch should return error response with exception message`() {
        // Arrange
        val jwtTenantId = "tenant-jwt-123"
        val contextTenantId = "tenant-context-456"
        val exception = TenantMismatchException(jwtTenantId, contextTenantId)

        // Act
        val response = handler.handleTenantMismatch(exception)
        val body = response.body!!

        // Assert
        assertThat(body.message).contains(jwtTenantId)
        assertThat(body.message).contains(contextTenantId)
        assertThat(body.message).contains("does not match")
    }

    @Test
    fun `handleTenantMismatch should handle null jwtTenantId`() {
        // Arrange
        val exception = TenantMismatchException(null, "context-tenant")

        // Act
        val response = handler.handleTenantMismatch(exception)
        val body = response.body!!

        // Assert
        assertThat(body.message).contains("null")
        assertThat(body.message).contains("context-tenant")
    }

    @Test
    fun `handleTenantMismatch should handle null contextTenantId`() {
        // Arrange
        val exception = TenantMismatchException("jwt-tenant", null)

        // Act
        val response = handler.handleTenantMismatch(exception)
        val body = response.body!!

        // Assert
        assertThat(body.message).contains("jwt-tenant")
        assertThat(body.message).contains("null")
    }

    @Test
    fun `handleTenantMismatch should handle both null tenant ids`() {
        // Arrange
        val exception = TenantMismatchException(null, null)

        // Act
        val response = handler.handleTenantMismatch(exception)
        val body = response.body!!

        // Assert
        assertThat(body.message).contains("null")
    }

    @Test
    fun `ErrorResponse should have correct structure`() {
        // Arrange
        val status = 404
        val error = "Not Found"
        val message = "Resource not found"

        // Act
        val errorResponse = GlobalExceptionHandler.ErrorResponse(
            status = status,
            error = error,
            message = message
        )

        // Assert
        assertThat(errorResponse.status).isEqualTo(status)
        assertThat(errorResponse.error).isEqualTo(error)
        assertThat(errorResponse.message).isEqualTo(message)
    }

    @Test
    fun `ErrorResponse should be data class`() {
        // Arrange
        val response1 = GlobalExceptionHandler.ErrorResponse(401, "Unauthorized", "Test")
        val response2 = GlobalExceptionHandler.ErrorResponse(401, "Unauthorized", "Test")

        // Assert
        assertThat(response1).isEqualTo(response2)
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode())
    }
}
