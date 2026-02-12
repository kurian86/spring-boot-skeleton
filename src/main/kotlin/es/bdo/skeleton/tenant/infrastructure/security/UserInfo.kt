package es.bdo.skeleton.tenant.infrastructure.security

data class UserInfo(
    val subject: String,
    val username: String,
    val email: String,
    val issuer: String,
    val attributes: Map<String, Any> = emptyMap()
)
