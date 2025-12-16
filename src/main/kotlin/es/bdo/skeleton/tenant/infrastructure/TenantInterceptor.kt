package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.application.TenantProvider
import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class TenantInterceptor(
    private val provider: TenantProvider
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val tenantId = request.getHeader("X-Tenant-ID") ?: "default"

        if (tenantId.isBlank()) {
            throw TenantNotFoundException("Tenant ID cannot be blank")
        }

        val tenant = provider.findById(tenantId)
            ?: throw TenantNotFoundException("Tenant not found: $tenantId")

        if (!tenant.isActive) {
            provider.evictTenant(tenantId)
            throw TenantNotFoundException("Tenant is not active: $tenantId")
        }

        TenantContext.tenantId = tenantId
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        TenantContext.clear()
    }
}
