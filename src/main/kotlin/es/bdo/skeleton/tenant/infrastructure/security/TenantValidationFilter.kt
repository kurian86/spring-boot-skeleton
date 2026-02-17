package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.application.exception.TenantMismatchException
import es.bdo.skeleton.tenant.application.security.UserInfo
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that validates the tenant_id from the authenticated JWT token
 * matches the tenant in TenantContext. This filter runs after authentication
 * to leverage Spring Security's JWT parsing and verification.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // Run after TenantContextFilter but early
class TenantValidationFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication

        // Only validate if we have a JWT authentication with UserInfo principal
        if (authentication?.principal is UserInfo) {
            val userInfo = authentication.principal as UserInfo
            val contextTenantId = TenantContext.getOrNull()
            val jwtTenantId = userInfo.attributes["tenant_id"] as? String

            // Validate tenant_id from JWT matches context tenant
            if (jwtTenantId != null && contextTenantId != null && jwtTenantId != contextTenantId) {
                throw TenantMismatchException(jwtTenantId, contextTenantId)
            }
        }

        filterChain.doFilter(request, response)
    }
}
