package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.application.security.UserInfo
import es.bdo.skeleton.user.application.UserRegistrationService
import es.bdo.skeleton.user.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant

class TenantJwtAuthenticationConverterTest {

    private lateinit var userRegistrationService: UserRegistrationService
    private lateinit var converter: TenantJwtAuthenticationConverter

    @BeforeEach
    fun setUp() {
        userRegistrationService = mock(UserRegistrationService::class.java)
        converter = TenantJwtAuthenticationConverter(userRegistrationService)
    }

    private fun createJwt(claims: Map<String, Any>): Jwt {
        return Jwt.withTokenValue("test-token")
            .header("alg", "RS256")
            .subject(claims["sub"] as? String ?: "user-123")
            .issuer(claims["iss"] as? String ?: "https://auth.example.com")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .also { builder ->
                claims.forEach { (key, value) ->
                    if (key != "sub" && key != "iss") {
                        builder.claim(key, value)
                    }
                }
            }
            .build()
    }

    private fun mockUser(): User {
        return mock(User::class.java)
    }

    @Test
    fun `should convert jwt to authentication token`() {
        // Arrange
        val claims = mapOf(
            "sub" to "user-123",
            "email" to "user@example.com",
            "preferred_username" to "testuser",
            "name" to "Test User",
            "roles" to listOf("USER", "ADMIN"),
            "tenant_id" to "tenant-1"
        )
        val jwt = createJwt(claims)

        `when`(userRegistrationService.ensureUserExists("user@example.com", "Test User", "user-123"))
            .thenReturn(mockUser())

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        assertThat(authentication).isInstanceOf(TenantJwtAuthenticationToken::class.java)
        assertThat(authentication.isAuthenticated).isTrue()
        assertThat(authentication.principal).isInstanceOf(UserInfo::class.java)
        
        val userInfo = authentication.principal as UserInfo
        assertThat(userInfo.subject).isEqualTo("user-123")
        assertThat(userInfo.email).isEqualTo("user@example.com")
        assertThat(userInfo.username).isEqualTo("testuser")
    }

    @Test
    fun `should extract authorities from roles claim`() {
        // Arrange
        val claims = mapOf(
            "sub" to "user-123",
            "email" to "user@example.com",
            "preferred_username" to "testuser",
            "roles" to listOf("USER", "ADMIN", "MANAGER")
        )
        val jwt = createJwt(claims)

        `when`(userRegistrationService.ensureUserExists("user@example.com", "testuser", "user-123"))
            .thenReturn(mockUser())

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        val authorities = authentication.authorities.map { it.authority }
        assertThat(authorities).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER")
    }

    @Test
    fun `should call user registration service with correct parameters`() {
        // Arrange
        val claims = mapOf(
            "sub" to "user-456",
            "email" to "newuser@example.com",
            "preferred_username" to "newuser",
            "name" to "New User"
        )
        val jwt = createJwt(claims)

        `when`(userRegistrationService.ensureUserExists("newuser@example.com", "New User", "user-456"))
            .thenReturn(mockUser())

        // Act
        converter.convert(jwt)

        // Assert - verify called with correct params
        org.mockito.Mockito.verify(userRegistrationService).ensureUserExists("newuser@example.com", "New User", "user-456")
    }

    @Test
    fun `should fallback to username when name attribute not present`() {
        // Arrange
        val claims = mapOf(
            "sub" to "user-789",
            "email" to "fallback@example.com",
            "preferred_username" to "fallbackuser"
            // No "name" attribute
        )
        val jwt = createJwt(claims)

        `when`(userRegistrationService.ensureUserExists("fallback@example.com", "fallbackuser", "user-789"))
            .thenReturn(mockUser())

        // Act
        converter.convert(jwt)

        // Assert
        org.mockito.Mockito.verify(userRegistrationService).ensureUserExists("fallback@example.com", "fallbackuser", "user-789")
    }

    @Test
    fun `should handle empty roles`() {
        // Arrange
        val claims = mapOf(
            "sub" to "user-000",
            "email" to "noroles@example.com",
            "preferred_username" to "norolesuser",
            "roles" to emptyList<String>()
        )
        val jwt = createJwt(claims)

        `when`(userRegistrationService.ensureUserExists("noroles@example.com", "norolesuser", "user-000"))
            .thenReturn(mockUser())

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        assertThat(authentication.authorities).isEmpty()
    }

    @Test
    fun `should handle missing roles claim`() {
        // Arrange
        val claims = mapOf(
            "sub" to "user-111",
            "email" to "norolesclaim@example.com",
            "preferred_username" to "user"
            // No "roles" claim
        )
        val jwt = createJwt(claims)

        `when`(userRegistrationService.ensureUserExists("norolesclaim@example.com", "user", "user-111"))
            .thenReturn(mockUser())

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        assertThat(authentication.authorities).isEmpty()
    }

    @Test
    fun `should preserve all jwt claims in userInfo attributes`() {
        // Arrange
        val claims = mapOf(
            "sub" to "user-222",
            "email" to "complete@example.com",
            "preferred_username" to "completeuser",
            "name" to "Complete User",
            "department" to "Engineering",
            "location" to "Madrid",
            "custom_claim" to "custom_value",
            "roles" to listOf("USER")
        )
        val jwt = createJwt(claims)

        `when`(userRegistrationService.ensureUserExists("complete@example.com", "Complete User", "user-222"))
            .thenReturn(mockUser())

        // Act
        val authentication = converter.convert(jwt)
        val userInfo = authentication.principal as UserInfo

        // Assert
        assertThat(userInfo.attributes).containsEntry("department", "Engineering")
        assertThat(userInfo.attributes).containsEntry("location", "Madrid")
        assertThat(userInfo.attributes).containsEntry("custom_claim", "custom_value")
    }

    @Test
    fun `should use sub as username fallback when no other username claim present`() {
        // Arrange
        val claims = mapOf(
            "sub" to "user-sub-123",
            "email" to "fallbacksub@example.com"
            // No preferred_username, username, login, or name
        )
        val jwt = createJwt(claims)

        `when`(userRegistrationService.ensureUserExists("fallbacksub@example.com", "user-sub-123", "user-sub-123"))
            .thenReturn(mockUser())

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        org.mockito.Mockito.verify(userRegistrationService).ensureUserExists("fallbacksub@example.com", "user-sub-123", "user-sub-123")
        val userInfo = authentication.principal as UserInfo
        assertThat(userInfo.username).isEqualTo("user-sub-123")
    }

    @Test
    fun `should handle username extraction priority order`() {
        // Arrange - preferred_username should take priority
        val claims = mapOf(
            "sub" to "user-333",
            "email" to "priority@example.com",
            "preferred_username" to "preferred",
            "username" to "username",
            "login" to "login",
            "name" to "Name",
            "roles" to listOf("USER")
        )
        val jwt = createJwt(claims)

        `when`(userRegistrationService.ensureUserExists("priority@example.com", "preferred", "user-333"))
            .thenReturn(mockUser())

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        val userInfo = authentication.principal as UserInfo
        assertThat(userInfo.username).isEqualTo("preferred")
    }

    @Test
    fun `should convert authorities to uppercase`() {
        // Arrange
        val claims = mapOf(
            "sub" to "user-444",
            "email" to "uppercase@example.com",
            "preferred_username" to "upperuser",
            "roles" to listOf("user", "Admin", "MANAGER")  // Mixed case
        )
        val jwt = createJwt(claims)

        `when`(userRegistrationService.ensureUserExists("uppercase@example.com", "upperuser", "user-444"))
            .thenReturn(mockUser())

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        val authorities = authentication.authorities.map { it.authority }
        assertThat(authorities).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER")
    }
}
