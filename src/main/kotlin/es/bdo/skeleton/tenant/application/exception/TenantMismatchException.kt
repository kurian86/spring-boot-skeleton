package es.bdo.skeleton.tenant.application.exception

/**
 * Exception thrown when the tenant_id in the JWT token does not match
 * the tenant in the request context (X-Tenant-ID header).
 */
class TenantMismatchException(
    val jwtTenantId: String,
    val contextTenantId: String
) : RuntimeException("JWT tenant_id '$jwtTenantId' does not match context tenant '$contextTenantId'")