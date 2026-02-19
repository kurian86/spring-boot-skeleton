package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.application.security.UserInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class TenantJwtAuthenticationTokenTest {

    @Test
    fun `should create token with jwt authorities and userInfo`() {
        // Arrange
        val jwt = mock(Jwt::class.java)
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN"))
        val userInfo = UserInfo(
            subject = "user-123",
            username = "testuser",
            email = "test@example.com",
            issuer = "https://auth.example.com",
            attributes = mapOf("name" to "Test User")
        )

        // Act
        val token = TenantJwtAuthenticationToken(jwt, authorities, userInfo)

        // Assert
        assertThat(token.principal).isEqualTo(userInfo)
        assertThat(token.authorities).hasSize(2)
        assertThat(token.authorities.map { it.authority }).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
        assertThat(token.isAuthenticated).isTrue()
    }

    @Test
    fun `should return userInfo as principal`() {
        // Arrange
        val jwt = mock(Jwt::class.java)
        val authorities = emptyList<SimpleGrantedAuthority>()
        val userInfo = UserInfo(
            subject = "user-456",
            username = "anotheruser",
            email = "another@example.com",
            issuer = "https://auth.example.com"
        )

        // Act
        val token = TenantJwtAuthenticationToken(jwt, authorities, userInfo)

        // Assert
        val principal = token.principal
        assertThat(principal).isInstanceOf(UserInfo::class.java)
        assertThat(principal.subject).isEqualTo("user-456")
        assertThat(principal.username).isEqualTo("anotheruser")
        assertThat(principal.email).isEqualTo("another@example.com")
    }

    @Test
    fun `should be authenticated by default`() {
        // Arrange
        val jwt = mock(Jwt::class.java)
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val userInfo = UserInfo(
            subject = "user-789",
            username = "user",
            email = "user@example.com",
            issuer = "https://auth.example.com"
        )

        // Act
        val token = TenantJwtAuthenticationToken(jwt, authorities, userInfo)

        // Assert
        assertThat(token.isAuthenticated).isTrue()
    }

    @Test
    fun `should handle empty authorities`() {
        // Arrange
        val jwt = mock(Jwt::class.java)
        val authorities = emptyList<SimpleGrantedAuthority>()
        val userInfo = UserInfo(
            subject = "user-000",
            username = "nouser",
            email = "nouser@example.com",
            issuer = "https://auth.example.com"
        )

        // Act
        val token = TenantJwtAuthenticationToken(jwt, authorities, userInfo)

        // Assert
        assertThat(token.authorities).isEmpty()
        assertThat(token.isAuthenticated).isTrue()
    }

    @Test
    fun `should store jwt reference`() {
        // Arrange
        val jwt = mock(Jwt::class.java)
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val userInfo = UserInfo(
            subject = "user-111",
            username = "test",
            email = "test@test.com",
            issuer = "https://auth.example.com"
        )

        // Act
        val token = TenantJwtAuthenticationToken(jwt, authorities, userInfo)

        // Assert
        assertThat(token.token).isEqualTo(jwt)
    }

    @Test
    fun `should preserve userInfo attributes`() {
        // Arrange
        val jwt = mock(Jwt::class.java)
        val authorities = emptyList<SimpleGrantedAuthority>()
        val attributes = mapOf(
            "tenant_id" to "tenant-123",
            "department" to "Engineering",
            "level" to "Senior"
        )
        val userInfo = UserInfo(
            subject = "user-222",
            username = "devuser",
            email = "dev@example.com",
            issuer = "https://auth.example.com",
            attributes = attributes
        )

        // Act
        val token = TenantJwtAuthenticationToken(jwt, authorities, userInfo)

        // Assert
        assertThat(token.principal.attributes).containsEntry("tenant_id", "tenant-123")
        assertThat(token.principal.attributes).containsEntry("department", "Engineering")
        assertThat(token.principal.attributes).containsEntry("level", "Senior")
    }
}
