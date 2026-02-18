package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.application.exception.TenantMismatchException
import es.bdo.skeleton.tenant.application.security.UserInfo
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TenantValidationFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication?.principal is UserInfo) {
            val userInfo = authentication.principal as UserInfo
            val contextTenantId = TenantContext.getOrNull()
            val jwtTenantId = userInfo.attributes["tenant_id"] as? String

            if (jwtTenantId != contextTenantId) {
                throw TenantMismatchException(jwtTenantId, contextTenantId)
            }
        }

        filterChain.doFilter(request, response)
    }
}
