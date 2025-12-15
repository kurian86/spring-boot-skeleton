package es.bdo.skeleton.main.tenant

data class TenantConfig(
    val tenantId: String,
    val dbUrl: String,
    val dbUsername: String,
    val dbPassword: String
)
