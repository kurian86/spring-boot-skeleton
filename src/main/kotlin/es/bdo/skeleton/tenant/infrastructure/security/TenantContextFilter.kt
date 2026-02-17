package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
import es.bdo.skeleton.tenant.domain.TenantRepository
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
    private val repository: TenantRepository,
) : OncePerRequestFilter() {

    companion object {
        private const val TENANT_HEADER = "X-Tenant-ID"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val tenantId = extractAndValidateTenantId(request)

        TenantContext.withTenant(tenantId) {
            filterChain.doFilter(request, response)
        }
    }

    private fun extractAndValidateTenantId(request: HttpServletRequest): String {
        val tenantId = request.getHeader(TENANT_HEADER) ?: TenantContext.DEFAULT_TENANT
        
        if (tenantId.isBlank()) {
            throw TenantNotFoundException("Tenant ID cannot be blank")
        }

        val tenant = repository.findById(tenantId)
            ?: throw TenantNotFoundException("Tenant not found: $tenantId")

        if (!tenant.isActive) {
            repository.evictCache(tenantId)
            throw TenantNotFoundException("Tenant is not active: $tenantId")
        }

        return tenantId
    }
}
