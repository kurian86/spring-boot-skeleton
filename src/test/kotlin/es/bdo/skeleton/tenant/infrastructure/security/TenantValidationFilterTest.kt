package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.application.exception.TenantMismatchException
import es.bdo.skeleton.tenant.application.security.UserInfo
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl

class TenantValidationFilterTest {

    private lateinit var filter: TenantValidationFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var filterChain: FilterChain

    @BeforeEach
    fun setUp() {
        filter = TenantValidationFilter()
        request = mock()
        response = mock()
        filterChain = mock()
        
        // Clear security context and tenant context before each test
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should allow request when JWT tenant matches context tenant`() {
        // Given
        val userInfo = createUserInfo(tenantId = "default")
        setupAuthentication(userInfo)
        
        // Run in tenant context
        TenantContext.withTenant("default") {
            filter.doFilter(request, response, filterChain)
        }

        // Then
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should throw TenantMismatchException when JWT tenant does not match context tenant`() {
        // Given
        val userInfo = createUserInfo(tenantId = "tenant-a")
        setupAuthentication(userInfo)
        
        // When/Then - Should throw exception when tenants don't match
        val exception = assertThrows<TenantMismatchException> {
            TenantContext.withTenant("tenant-b") {
                filter.doFilter(request, response, filterChain)
            }
        }
        
        // Verify exception contains correct info
        assert(exception.jwtTenantId == "tenant-a")
        assert(exception.contextTenantId == "tenant-b")
    }

    @Test
    fun `should allow request when no authentication`() {
        // Given - No authentication in context
        SecurityContextHolder.clearContext()
        
        TenantContext.withTenant("default") {
            filter.doFilter(request, response, filterChain)
        }

        // Then
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should allow request when authentication principal is not UserInfo`() {
        // Given
        val auth = mock<Authentication>()
        whenever(auth.principal).thenReturn("some-other-principal")
        SecurityContextHolder.setContext(SecurityContextImpl(auth))
        
        TenantContext.withTenant("default") {
            filter.doFilter(request, response, filterChain)
        }

        // Then
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should allow request when no tenant_id in JWT`() {
        // Given
        val userInfo = UserInfo(
            subject = "123",
            username = "test",
            email = "test@test.com",
            issuer = "http://localhost:8080",
            attributes = mapOf() // No tenant_id
        )
        setupAuthentication(userInfo)
        
        TenantContext.withTenant("default") {
            filter.doFilter(request, response, filterChain)
        }

        // Then
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should allow request when no tenant in context`() {
        // Given
        val userInfo = createUserInfo(tenantId = "default")
        setupAuthentication(userInfo)
        
        // No tenant context set - filter should still work
        filter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
    }

    private fun createUserInfo(tenantId: String): UserInfo {
        return UserInfo(
            subject = "123",
            username = "test",
            email = "test@test.com",
            issuer = "http://localhost:8080",
            attributes = mapOf("tenant_id" to tenantId)
        )
    }

    private fun setupAuthentication(userInfo: UserInfo) {
        val auth = mock<Authentication>()
        whenever(auth.principal).thenReturn(userInfo)
        SecurityContextHolder.setContext(SecurityContextImpl(auth))
    }
}
