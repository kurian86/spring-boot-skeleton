package es.bdo.skeleton.shared.exception

import es.bdo.skeleton.tenant.application.exception.InvalidTokenException
import es.bdo.skeleton.tenant.application.exception.TenantNotConfiguredException
import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
import es.bdo.skeleton.user.application.exception.EmailDomainNotAllowedException
import es.bdo.skeleton.user.application.exception.UserAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TenantNotFoundException::class)
    fun handleTenantNotFound(ex: TenantNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Unauthorized",
                    message = ex.message ?: "Tenant not found"
                )
            )
    }

    @ExceptionHandler(TenantNotConfiguredException::class)
    fun handleTenantNotConfigured(ex: TenantNotConfiguredException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse(
                    status = HttpStatus.FORBIDDEN.value(),
                    error = "Forbidden",
                    message = ex.message ?: "Tenant not configured for registration"
                )
            )
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(ex: InvalidTokenException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Unauthorized",
                    message = ex.message ?: "Invalid authentication token"
                )
            )
    }

    @ExceptionHandler(EmailDomainNotAllowedException::class)
    fun handleEmailDomainNotAllowed(ex: EmailDomainNotAllowedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse(
                    status = HttpStatus.FORBIDDEN.value(),
                    error = "Forbidden",
                    message = ex.message ?: "Email domain not allowed for this tenant"
                )
            )
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(ex: UserAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    status = HttpStatus.CONFLICT.value(),
                    error = "Conflict",
                    message = ex.message ?: "User already exists"
                )
            )
    }

    data class ErrorResponse(
        val status: Int,
        val error: String,
        val message: String
    )
}
