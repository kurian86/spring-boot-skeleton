package es.bdo.skeleton.tenant.infrastructure.security

data class UserInfo(
    val subject: String,
    val username: String,
    val email: String,
    val issuer: String,
    val attributes: Map<String, Any> = emptyMap()
) {

    companion object {
        fun fromAttributes(attributes: Map<String, Any>): UserInfo {
            val subject = attributes["sub"] as? String ?: ""
            val username = extractUsername(attributes)
            val email = attributes["email"] as? String ?: ""
            val issuer = attributes["iss"] as? String ?: ""

            return UserInfo(
                subject = subject,
                username = username,
                email = email,
                issuer = issuer,
                attributes = attributes
            )
        }

        private fun extractUsername(attributes: Map<String, Any>): String {
            return (attributes["preferred_username"]
                ?: attributes["username"]
                ?: attributes["login"]
                ?: attributes["name"]
                ?: attributes["sub"]
                ?: "") as String
        }
    }
}
