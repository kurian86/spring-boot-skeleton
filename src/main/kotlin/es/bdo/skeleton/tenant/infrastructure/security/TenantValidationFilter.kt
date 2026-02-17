package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.application.security.UserInfo
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that validates the tenant_id from the authenticated JWT token
 * matches the X-Tenant-ID header. This filter runs after authentication
 * to leverage Spring Security's JWT parsing and verification.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // Run after TenantContextFilter but early
class TenantValidationFilter : OncePerRequestFilter() {

    companion object {
        private const val TENANT_HEADER = "X-Tenant-ID"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication

        // Only validate if we have a JWT authentication with UserInfo principal
        if (authentication?.principal is UserInfo) {
            val userInfo = authentication.principal as UserInfo
            val headerTenantId = request.getHeader(TENANT_HEADER)
            val jwtTenantId = userInfo.attributes["tenant_id"] as? String

            // Validate tenant_id from JWT matches header
            if (jwtTenantId != null && headerTenantId != null && jwtTenantId != headerTenantId) {
                response.status = HttpStatus.FORBIDDEN.value()
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.writer.write(
                    """{"error": "Tenant mismatch", "message": "JWT tenant_id '$jwtTenantId' does not match X-Tenant-ID header '$headerTenantId'"}"""
                )
                return
            }
        }

        filterChain.doFilter(request, response)
    }
}
