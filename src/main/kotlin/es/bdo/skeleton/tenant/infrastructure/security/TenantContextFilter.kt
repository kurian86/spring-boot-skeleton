package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.application.TenantProvider
import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TenantContextFilter(
    private val tenantProvider: TenantProvider
) : OncePerRequestFilter() {

    companion object {
        private const val TENANT_HEADER = "X-Tenant-ID"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val tenantId = extractTenantId(request)
            validateAndSetTenant(tenantId)
            filterChain.doFilter(request, response)
        } finally {
            TenantContext.clear()
        }
    }

    private fun extractTenantId(request: HttpServletRequest): String {
        return request.getHeader(TENANT_HEADER) ?: TenantContext.DEFAULT_TENANT
    }

    private fun validateAndSetTenant(tenantId: String) {
        if (tenantId.isBlank()) {
            throw TenantNotFoundException("Tenant ID cannot be blank")
        }

        val tenant = tenantProvider.findById(tenantId)
            ?: throw TenantNotFoundException("Tenant not found: $tenantId")

        if (!tenant.isActive) {
            tenantProvider.evictTenant(tenantId)
            throw TenantNotFoundException("Tenant is not active: $tenantId")
        }

        TenantContext.tenantId = tenantId
    }
}
