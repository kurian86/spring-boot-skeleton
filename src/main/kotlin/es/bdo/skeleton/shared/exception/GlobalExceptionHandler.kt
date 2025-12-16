package es.bdo.skeleton.shared.exception

import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
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

    data class ErrorResponse(
        val status: Int,
        val error: String,
        val message: String
    )
}
