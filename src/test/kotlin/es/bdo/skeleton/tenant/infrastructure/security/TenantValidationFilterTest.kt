package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.application.exception.TenantMismatchException
import es.bdo.skeleton.tenant.application.security.UserInfo
import es.bdo.skeleton.tenant.infrastructure.security.jwt.TenantJwtAuthenticationToken
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

class TenantValidationFilterTest {

    @Mock
    private lateinit var request: HttpServletRequest

    @Mock
    private lateinit var response: HttpServletResponse

    @Mock
    private lateinit var filterChain: FilterChain

    @Mock
    private lateinit var jwt: Jwt

    private lateinit var filter: TenantValidationFilter

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        filter = TenantValidationFilter()
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    private fun createUserInfo(tenantId: String?): UserInfo {
        return UserInfo(
            subject = "user-123",
            username = "testuser",
            email = "test@example.com",
            issuer = "https://auth.example.com",
            attributes = if (tenantId != null) mapOf("tenant_id" to tenantId) else emptyMap()
        )
    }

    private fun createAuthentication(userInfo: UserInfo): Authentication {
        return TenantJwtAuthenticationToken(
            jwt,
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            userInfo
        )
    }

    @Test
    fun `should continue filter chain when tenant matches`() {
        // Arrange
        val userInfo = createUserInfo("tenant-1")
        val authentication = createAuthentication(userInfo)
        SecurityContextHolder.getContext().authentication = authentication

        TenantContext.withTenant("tenant-1") {
            // Act
            filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
        }

        // Assert
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should throw exception when jwt tenant does not match context tenant`() {
        // Arrange
        val userInfo = createUserInfo("tenant-jwt")
        val authentication = createAuthentication(userInfo)
        SecurityContextHolder.getContext().authentication = authentication

        // Act & Assert
        TenantContext.withTenant("tenant-context") {
            assertThatThrownBy {
                filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
            }.isInstanceOf(TenantMismatchException::class.java)
                .hasMessageContaining("JWT tenant_id 'tenant-jwt' does not match context tenant 'tenant-context'")
        }
    }

    @Test
    fun `should throw exception when jwt tenant is null and context is not`() {
        // Arrange
        val userInfo = createUserInfo(null)  // No tenant_id in JWT
        val authentication = createAuthentication(userInfo)
        SecurityContextHolder.getContext().authentication = authentication

        // Act & Assert
        TenantContext.withTenant("tenant-1") {
            assertThatThrownBy {
                filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
            }.isInstanceOf(TenantMismatchException::class.java)
                .hasMessageContaining("JWT tenant_id 'null' does not match context tenant 'tenant-1'")
        }
    }

    @Test
    fun `should throw exception when context tenant is null and jwt tenant is not`() {
        // Arrange
        val userInfo = createUserInfo("tenant-jwt")
        val authentication = createAuthentication(userInfo)
        SecurityContextHolder.getContext().authentication = authentication

        // Act - No tenant context set
        // Assert
        assertThatThrownBy {
            filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
        }.isInstanceOf(TenantMismatchException::class.java)
            .hasMessageContaining("JWT tenant_id 'tenant-jwt' does not match context tenant 'null'")
    }

    @Test
    fun `should continue when both tenants are null`() {
        // Arrange
        val userInfo = createUserInfo(null)
        val authentication = createAuthentication(userInfo)
        SecurityContextHolder.getContext().authentication = authentication

        // Act - No tenant context set
        filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)

        // Assert
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should skip validation when authentication is null`() {
        // Arrange - No authentication in context
        SecurityContextHolder.clearContext()

        // Act
        filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)

        // Assert
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should skip validation when principal is not UserInfo`() {
        // Arrange - Authentication with different principal type
        val nonUserInfoAuth = mock(Authentication::class.java)
        `when`(nonUserInfoAuth.principal).thenReturn("string-principal")
        SecurityContextHolder.getContext().authentication = nonUserInfoAuth

        // Act
        filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)

        // Assert
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should work with different tenant id formats`() {
        // Arrange
        val tenantIds = listOf(
            "tenant-123",
            "company-abc",
            "org.example.tenant",
            "tenant_456"
        )

        tenantIds.forEach { tenantId ->
            val userInfo = createUserInfo(tenantId)
            val authentication = createAuthentication(userInfo)
            SecurityContextHolder.getContext().authentication = authentication

            // Act & Assert
            TenantContext.withTenant(tenantId) {
                filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
            }

            // Verify filter chain was called
            verify(filterChain, atLeastOnce()).doFilter(request, response)
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    fun `should include tenant ids in exception when mismatch occurs`() {
        // Arrange
        val userInfo = createUserInfo("jwt-tenant-xyz")
        val authentication = createAuthentication(userInfo)
        SecurityContextHolder.getContext().authentication = authentication

        // Act & Assert
        TenantContext.withTenant("context-tenant-abc") {
            assertThatThrownBy {
                filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
            }.isInstanceOfSatisfying(TenantMismatchException::class.java) { ex ->
                assertThat(ex.jwtTenantId).isEqualTo("jwt-tenant-xyz")
                assertThat(ex.contextTenantId).isEqualTo("context-tenant-abc")
            }
        }
    }

    @Test
    fun `should not call filter chain when validation fails`() {
        // Arrange
        val userInfo = createUserInfo("tenant-a")
        val authentication = createAuthentication(userInfo)
        SecurityContextHolder.getContext().authentication = authentication

        // Act & Assert
        TenantContext.withTenant("tenant-b") {
            assertThatThrownBy {
                filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
            }.isInstanceOf(TenantMismatchException::class.java)
        }

        // Filter chain should not be called when validation fails
        verify(filterChain, never()).doFilter(request, response)
    }

    @Test
    fun `should handle case sensitive tenant ids`() {
        // Arrange
        val userInfo = createUserInfo("Tenant-ABC")
        val authentication = createAuthentication(userInfo)
        SecurityContextHolder.getContext().authentication = authentication

        // Act & Assert - Different case should fail
        TenantContext.withTenant("tenant-abc") {
            assertThatThrownBy {
                filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
            }.isInstanceOf(TenantMismatchException::class.java)
        }

        // But same case should pass
        TenantContext.withTenant("Tenant-ABC") {
            filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
        }

        verify(filterChain, atLeastOnce()).doFilter(request, response)
    }
}
