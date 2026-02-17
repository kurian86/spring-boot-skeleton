package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.domain.Tenant
import es.bdo.skeleton.tenant.domain.TenantRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.io.PrintWriter
import java.util.Base64

class TenantContextFilterTest {

    private lateinit var filter: TenantContextFilter
    private lateinit var repository: TenantRepository
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var filterChain: FilterChain
    private lateinit var printWriter: PrintWriter

    @BeforeEach
    fun setUp() {
        repository = mock()
        filter = TenantContextFilter(repository)
        request = mock()
        response = mock()
        filterChain = mock()
        printWriter = mock()
        whenever(response.writer).thenReturn(printWriter)
    }

    @Test
    fun `should allow request when JWT tenant matches header`() {
        // Given
        val jwt = createValidJwt(tenantId = "default")
        whenever(request.getHeader("Authorization")).thenReturn("Bearer $jwt")
        whenever(request.getHeader("X-Tenant-ID")).thenReturn("default")
        whenever(repository.findById("default")).thenReturn(createTenant("default", true))

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should reject request when JWT tenant does not match header`() {
        // Given
        val jwt = createValidJwt(tenantId = "tenant-a")
        whenever(request.getHeader("Authorization")).thenReturn("Bearer $jwt")
        whenever(request.getHeader("X-Tenant-ID")).thenReturn("tenant-b")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(response).status = 403
        verify(filterChain, never()).doFilter(any(), any())
    }

    @Test
    fun `should use header tenant when no JWT`() {
        // Given
        whenever(request.getHeader("Authorization")).thenReturn(null)
        whenever(request.getHeader("X-Tenant-ID")).thenReturn("default")
        whenever(repository.findById("default")).thenReturn(createTenant("default", true))

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should throw exception when tenant not found`() {
        // Given
        whenever(request.getHeader("Authorization")).thenReturn(null)
        whenever(request.getHeader("X-Tenant-ID")).thenReturn("unknown")
        whenever(repository.findById("unknown")).thenReturn(null)

        // When/Then
        assertThrows<es.bdo.skeleton.tenant.application.exception.TenantNotFoundException> {
            filter.doFilter(request, response, filterChain)
        }
    }

    private fun createValidJwt(tenantId: String): String {
        // Create a simple JWT for testing (unsigned)
        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        val payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
            """{"sub":"123","email":"test@test.com","tenant_id":"$tenantId","roles":["user"]}"""
                .toByteArray()
        )
        return "$header.$payload.signature"
    }

    private fun createTenant(id: String, active: Boolean): Tenant {
        return Tenant(
            id = id,
            name = id,
            dbDatabase = "test",
            dbUsername = "test",
            dbPassword = "test",
            isActive = active,
            createdAt = java.time.ZonedDateTime.now(),
            updatedAt = java.time.ZonedDateTime.now()
        )
    }
}
