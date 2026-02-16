package es.bdo.skeleton.tenant.application

import java.lang.ScopedValue

object TenantContext {
    const val DEFAULT_TENANT = "default"
    
    private val TENANT: ScopedValue<String> = ScopedValue.newInstance()

    val tenantId: String
        get() = if (TENANT.isBound) TENANT.get() else DEFAULT_TENANT

    fun isBound(): Boolean = TENANT.isBound

    fun getOrNull(): String? = if (TENANT.isBound) TENANT.get() else null

    fun <T> withTenant(tenantId: String, operation: () -> T): T {
        return ScopedValue.where(TENANT, tenantId).call(
            ScopedValue.CallableOp<T, RuntimeException> { operation() }
        )
    }
}
