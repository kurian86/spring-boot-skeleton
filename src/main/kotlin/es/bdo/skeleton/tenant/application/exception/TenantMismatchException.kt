package es.bdo.skeleton.tenant.application.exception

class TenantMismatchException(
    val jwtTenantId: String?,
    val contextTenantId: String?
) : RuntimeException("JWT tenant_id '$jwtTenantId' does not match context tenant '$contextTenantId'")
