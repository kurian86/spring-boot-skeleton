package es.bdo.skeleton.tenant.application.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserInfoTest {

    // --- data class construction ---

    @Test
    fun `should store all fields passed to constructor`() {
        // Arrange
        val attributes = mapOf("sub" to "u1", "custom" to "value")

        // Act
        val userInfo = UserInfo(
            subject = "u1",
            username = "alice",
            email = "alice@example.com",
            issuer = "https://auth.example.com",
            attributes = attributes
        )

        // Assert
        assertThat(userInfo.subject).isEqualTo("u1")
        assertThat(userInfo.username).isEqualTo("alice")
        assertThat(userInfo.email).isEqualTo("alice@example.com")
        assertThat(userInfo.issuer).isEqualTo("https://auth.example.com")
        assertThat(userInfo.attributes).isEqualTo(attributes)
    }

    @Test
    fun `should default attributes to empty map when not provided`() {
        // Arrange & Act
        val userInfo = UserInfo(
            subject = "u1",
            username = "alice",
            email = "alice@example.com",
            issuer = "https://auth.example.com"
        )

        // Assert
        assertThat(userInfo.attributes).isEmpty()
    }

    // --- fromAttributes happy path ---

    @Test
    fun `fromAttributes maps all standard JWT claims when present`() {
        // Arrange
        val attributes = mapOf(
            "sub" to "user-123",
            "preferred_username" to "alice",
            "email" to "alice@example.com",
            "iss" to "https://auth.example.com"
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.subject).isEqualTo("user-123")
        assertThat(userInfo.username).isEqualTo("alice")
        assertThat(userInfo.email).isEqualTo("alice@example.com")
        assertThat(userInfo.issuer).isEqualTo("https://auth.example.com")
    }

    @Test
    fun `fromAttributes stores original attributes map on the object`() {
        // Arrange
        val attributes = mapOf(
            "sub" to "user-123",
            "preferred_username" to "alice",
            "email" to "alice@example.com",
            "iss" to "https://auth.example.com",
            "tenant_id" to "acme",
            "roles" to listOf("USER", "ADMIN")
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.attributes).isEqualTo(attributes)
        assertThat(userInfo.attributes["tenant_id"]).isEqualTo("acme")
        assertThat(userInfo.attributes["roles"]).isEqualTo(listOf("USER", "ADMIN"))
    }

    // --- fromAttributes fallback to empty string ---

    @Test
    fun `fromAttributes defaults subject to empty string when sub is absent`() {
        // Arrange
        val attributes = mapOf(
            "preferred_username" to "alice",
            "email" to "alice@example.com",
            "iss" to "https://auth.example.com"
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.subject).isEmpty()
    }

    @Test
    fun `fromAttributes defaults email to empty string when email is absent`() {
        // Arrange
        val attributes = mapOf(
            "sub" to "user-123",
            "preferred_username" to "alice",
            "iss" to "https://auth.example.com"
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.email).isEmpty()
    }

    @Test
    fun `fromAttributes defaults issuer to empty string when iss is absent`() {
        // Arrange
        val attributes = mapOf(
            "sub" to "user-123",
            "preferred_username" to "alice",
            "email" to "alice@example.com"
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.issuer).isEmpty()
    }

    @Test
    fun `fromAttributes returns empty UserInfo when attributes map is empty`() {
        // Arrange
        val attributes = emptyMap<String, Any>()

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.subject).isEmpty()
        assertThat(userInfo.username).isEmpty()
        assertThat(userInfo.email).isEmpty()
        assertThat(userInfo.issuer).isEmpty()
        assertThat(userInfo.attributes).isEmpty()
    }

    // --- extractUsername priority chain ---

    @Test
    fun `fromAttributes uses preferred_username as username when present`() {
        // Arrange
        val attributes = mapOf(
            "sub" to "u1",
            "preferred_username" to "preferred",
            "username" to "username",
            "login" to "login",
            "name" to "name"
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.username).isEqualTo("preferred")
    }

    @Test
    fun `fromAttributes falls back to username when preferred_username is absent`() {
        // Arrange
        val attributes = mapOf(
            "sub" to "u1",
            "username" to "username-value",
            "login" to "login-value",
            "name" to "name-value"
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.username).isEqualTo("username-value")
    }

    @Test
    fun `fromAttributes falls back to login when preferred_username and username are absent`() {
        // Arrange
        val attributes = mapOf(
            "sub" to "u1",
            "login" to "login-value",
            "name" to "name-value"
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.username).isEqualTo("login-value")
    }

    @Test
    fun `fromAttributes falls back to name when preferred_username, username, and login are absent`() {
        // Arrange
        val attributes = mapOf(
            "sub" to "u1",
            "name" to "Full Name"
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.username).isEqualTo("Full Name")
    }

    @Test
    fun `fromAttributes falls back to sub when only sub is present`() {
        // Arrange
        val attributes = mapOf(
            "sub" to "user-sub-only"
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.username).isEqualTo("user-sub-only")
    }

    @Test
    fun `fromAttributes defaults username to empty string when no username claim is present`() {
        // Arrange
        val attributes = mapOf(
            "email" to "alice@example.com",
            "iss" to "https://auth.example.com"
        )

        // Act
        val userInfo = UserInfo.fromAttributes(attributes)

        // Assert
        assertThat(userInfo.username).isEmpty()
    }
}
