package es.bdo.skeleton.tenant.application

object TenantContext {
    const val DEFAULT_TENANT = "default"

    private val currentTenant = InheritableThreadLocal<String>()

    var tenantId: String?
        get() = currentTenant.get()
        set(value) = currentTenant.set(value)

    fun clear() {
        currentTenant.remove()
    }
}
