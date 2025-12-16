package es.bdo.skeleton.tenant.infrastructure

object TenantContext {
    private val currentTenant = ThreadLocal<String>()

    var tenantId: String?
        get() = currentTenant.get()
        set(value) = currentTenant.set(value)

    fun clear() {
        currentTenant.remove()
    }
}
