package es.bdo.skeleton.tenant.application.security

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
            val name = attributes["name"] as? String ?: username

            // Ensure name is included in attributes for auto-registration
            val enrichedAttributes = attributes + ("name" to name)

            return UserInfo(
                subject = subject,
                username = username,
                email = email,
                issuer = issuer,
                attributes = enrichedAttributes
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
