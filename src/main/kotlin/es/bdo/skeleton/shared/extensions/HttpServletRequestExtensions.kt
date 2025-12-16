package es.bdo.skeleton.shared.extensions

import es.bdo.skeleton.tenant.application.TenantContext
import jakarta.servlet.http.HttpServletRequest

val HttpServletRequest.tenantId: String?
    get() = TenantContext.tenantId

val HttpServletRequest.requireTenantId: String
    get() = TenantContext.tenantId
        ?: throw IllegalStateException("No tenant ID found in current context")
